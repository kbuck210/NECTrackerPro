$("#showMenulink").click(function() {
  $("#surface").toggleClass("nav-show"); 
  return false;
}); 

$("#showMenulink").click(function() {
  $("#interface").toggleClass("is-visible"); 
  return false;
}); 
 
$("#panelCloselink").click(function() {
  $("#surface").removeClass("nav-show");
  return false;
}); 

$("#panelCloselink").click(function() {
  $("#interface").removeClass("is-visible"); 
  return false;
}); 

$(".nav a").click(function() { 
    $("#surface").removeClass("nav-show");
    $("#interface").removeClass("is-visible"); 
  	return false;
});  

$(".nav a").click(function(event) { 
	$(".nav a").removeClass("is-active");
	var title = event.target.innerHTML;
	
	if (title === "Current Week") {
		$("#leaderboard-title").html(title + ': <small id="week-modifier">Week 17</small>');
	}
	else {
		$("#leaderboard-title").html(title);
	}
    $(this).toggleClass("is-active");
	event.preventDefault();
}); 
 