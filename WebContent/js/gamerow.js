$(".helmet-link").click(function() {
	var target = window.event.srcElement;
	var linkId = target.id.toString();
	var linkType = linkId.substring(0,11);
	var idNum = linkId.substring(11);
	
	var toggleId = "";
	var removeId = "";
	
	if (linkType === "home-helmet") {
		toggleId = "#home-badge" + idNum;
		removeId = "#away-badge" + idNum;
	}
	else if (linkType === "away-helmet") {
		toggleId = "#away-badge" + idNum;
		removeId = "#home-badge" + idNum;
	}
	
	if (toggleId.length > 0 && removeId.length > 0 && $(toggleId).hasClass("selectable")) {
		$(toggleId).toggleClass("selected");
  		$(removeId).removeClass("selected");
  		event.preventDefault();
	}
	
  	return false;
});