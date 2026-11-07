// Fills latitude/longitude fields from the browser's location, and hides the
// personal-location fields when a post is made on behalf of a shop.
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-fill-location]').forEach((button) => {
    button.addEventListener('click', () => {
      const form = button.closest('form');
      const status = (form && form.querySelector('[data-location-status]'))
        || document.querySelector('[data-location-status]');
      const say = (text) => { if (status) status.textContent = text; };
      if (!navigator.geolocation) {
        say('Your browser cannot share location. Please type it in.');
        return;
      }
      say('Finding your location…');
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          form.querySelector('[data-lat]').value = pos.coords.latitude.toFixed(5);
          form.querySelector('[data-lng]').value = pos.coords.longitude.toFixed(5);
          say('Location set.');
          if (button.hasAttribute('data-submit')) form.submit();
        },
        () => say('Could not get your location. Please allow location access or type it in.'),
        { enableHighAccuracy: false, timeout: 10000 }
      );
    });
  });

  const shopSelect = document.querySelector('[data-shop-select]');
  const personal = document.querySelector('[data-personal-only]');
  if (shopSelect && personal) {
    const sync = () => { personal.hidden = shopSelect.value !== ''; };
    shopSelect.addEventListener('change', sync);
    sync();
  }
});
