import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.example.smarttaskmanager.service.UserService;
import com.example.smarttaskmanager.security.JwtTokenProvider;
import com.example.smarttaskmanager.model.User;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username, request.password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.getUserByUsername(request.username);
            String token = jwtTokenProvider.generateToken(request.username, user.getRoles());

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (UsernameNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }

    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.username, request.password);
            return ResponseEntity.ok("User registered: " + user.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    public static class RegisterRequest {
        public String username;
        public String password;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public LoginResponse(String token) { this.token = token; }
    }
}
