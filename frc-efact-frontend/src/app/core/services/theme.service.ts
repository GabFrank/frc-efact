import { Injectable, signal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'theme-preference';
  darkMode = signal<boolean>(false);

  constructor() {
    this.initializeTheme();
    
    effect(() => {
      const isDark = this.darkMode();
      if (isDark) {
        document.body.classList.add('dark-theme');
        localStorage.setItem(this.THEME_KEY, 'dark');
      } else {
        document.body.classList.remove('dark-theme');
        localStorage.setItem(this.THEME_KEY, 'light');
      }
    });
  }

  private initializeTheme() {
    const savedTheme = localStorage.getItem(this.THEME_KEY);
    let isDark = false;
    
    if (savedTheme) {
      isDark = savedTheme === 'dark';
    } else {
      // Check system preference
      isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    
    // Apply theme immediately
    if (isDark) {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }
    
    // Set the signal value
    this.darkMode.set(isDark);
  }

  toggleTheme() {
    this.darkMode.update(current => !current);
  }
}
