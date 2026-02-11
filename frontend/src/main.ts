import { bootstrapApplication } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import { AppComponent } from './app/app';
bootstrapApplication(AppComponent, {
  providers: [
    provideAnimations(),
    provideToastr({
      // Global Toastr options
      positionClass: 'toast-top-right',  // top-right corner
      timeOut: 3000,                     // 3 seconds
      closeButton: true,                 // show close button
      progressBar: true,                 // show progress bar
      preventDuplicates: true            // prevent duplicate messages
    })
  ]
}).catch(err => console.error(err));
