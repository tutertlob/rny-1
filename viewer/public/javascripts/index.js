/**
 * 
 */
$(document).on('DOMContentLoaded', e => {
	$('.card-img-bottom').on('error', error => {
		$(error.target).next('.card-img-overlay').remove();
		$(error.target).remove();
	});
	
	$('.camera-selector').on('click', e => {
		const selector = $(e.currentTarget);
		const selected = $(e.currentTarget).data('cameraSelect');
		const h2 = selector.children();
		if (selector.hasClass('active')) {
			selector.removeClass('active');
			h2.removeClass('text-light').addClass('text-muted');
		} else {
			selector.addClass('active');
			h2.removeClass('text-muted').addClass('text-light');
		}
		
		$(`.camera-group-${selected}`).toggle();
	});

	$('#takePicBtn').on('click', e => {
		$.get('/images/capture')
		.done(response => {
			console.log(response);
			alert(response + '\n１分程待ってね');
		});
	});
	
	$('.delete-img-btn').on('click', e => {
		console.log(e);
		var img = $(e.target).data('deleteTarget');
		$.post('images/delete', {file: img})
		.done(response => {
			console.log('deleted');
			$(e.target).parents('.card').children('img').remove();
			$(e.target).parents('.card-img-overlay').remove();
			console.log(response);
		});
	});
});


