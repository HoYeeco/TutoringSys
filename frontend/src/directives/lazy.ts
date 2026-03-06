export const lazy = {
  mounted(el: HTMLElement, binding: any) {
    const imageUrl = binding.value;
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          if (el.tagName === 'IMG') {
            (el as HTMLImageElement).src = imageUrl;
          }
          observer.unobserve(el);
        }
      });
    });
    observer.observe(el);
  }
};
