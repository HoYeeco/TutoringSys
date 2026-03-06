export const watermark = {
  mounted(el: HTMLElement, binding: any) {
    const text = binding.value || 'Tutoring System';
    const canvas = document.createElement('canvas');
    canvas.width = 200;
    canvas.height = 150;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.rotate((-20 * Math.PI) / 180);
      ctx.font = '14px Arial';
      ctx.fillStyle = 'rgba(0, 0, 0, 0.1)';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(text, canvas.width / 2, canvas.height / 2);
    }
    const base64Url = canvas.toDataURL('image/png');
    el.style.backgroundImage = `url(${base64Url})`;
    el.style.backgroundRepeat = 'repeat';
  }
};
