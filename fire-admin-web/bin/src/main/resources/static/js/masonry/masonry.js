//  window.addEventListener('load', () => {
//    if ($('.masonry').length > 0) {
//      new Masonry('.masonry', {
//        itemSelector: '.masonry-item',
//        columnWidth: '.masonry-sizer',
//        percentPosition: true,
//      });
//    }
//  });


  window.addEventListener('load', function(){
	    if ($('.masonry').length > 0) {
	      new Masonry('.masonry', {
	        itemSelector: '.masonry-item',
	        columnWidth: '.masonry-sizer',
	        percentPosition: true,
	      });
	    }
	    
	    
	    
	  });