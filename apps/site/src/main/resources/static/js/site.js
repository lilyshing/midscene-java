// Import main JavaScript
fetch('/site/static/js/main.js')
  .then(response => response.text())
  .then(code => {
    try {
      eval(code);
    } catch (error) {
      console.warn('Error loading main.js:', error);
    }
  })
  .catch(error => {
    console.warn('Failed to load main.js:', error);
  });