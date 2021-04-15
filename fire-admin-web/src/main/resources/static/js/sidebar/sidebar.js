$(document).ready(function() {
//Sidebar links
  $('.sidebar .sidebar-menu li a').on('click', function () {
    const $this = $(this);

    if ($this.parent().hasClass('open')) {
      $this
        .parent()
        .children('.dropdown-menu')
        .slideUp(200, function() {
          $this.parent().removeClass('open');
        });
    } else {
      $this
        .parent()
        .parent()
        .children('li.open')
        .children('.dropdown-menu')
        .slideUp(200);

      $this
        .parent()
        .parent()
        .children('li.open')
        .children('a')
        .removeClass('open');

      $this
        .parent()
        .parent()
        .children('li.open')
        .removeClass('open');

//      $this
//        .parent()
//        .children('.dropdown-menu')
//        .slideDown(200, () => {
//          $this.parent().addClass('open');
//        });
      
      $this
      .parent()
      .children('.dropdown-menu')
      .slideDown(200, function() {
        $this.parent().addClass('open');
      });
      
    }
  });

  // Sidebar Activity Class
  const sidebarLinks = $('.sidebar').find('.sidebar-link');

//  sidebarLinks
//    .each((index, el) => {
//      $(el).removeClass('active');
//    })
  sidebarLinks
  .each(function(index, el) {
    $(el).removeClass('active');
  })
    .filter(function () {
      const href = $(this).attr('href');
      const pattern = href[0] === '/' ? href.substr(1) : href;
      return pattern === (window.location.pathname).substr(1);
    })
    .addClass('active');
  
  $('.sidebar-toggle').on('click', function(e){
	    $('.app').toggleClass('is-collapsed');
	    $('#fireImageLogo').toggle();
	    e.preventDefault();
	  });

  $(' .sidebar ').hover(function() {
	  if ($('.app').hasClass('is-collapsed')) {
		  $('#fireImageLogo').toggle();
	  }
	});
  
  /**
   * Wait untill sidebar fully toggled (animated in/out)
   * then trigger window resize event in order to recalculate
   * masonry layout widths and gutters.
   */
  
  $('#sidebar-toggle').click(function(e){
	    e.preventDefault();
	    setTimeout(function() {
	      window.dispatchEvent(window.EVENT);
	    }, 300);
	  });
  
} );
