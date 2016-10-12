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
	//	check first whether this is a single pick selection (i.e. unselect all other selectables)
	if (toggleId.length > 0 && $(toggleId).hasClass("selectable") && $(toggleId).hasClass("singlePick")) {
		//	First remove the 'selected' class from all selectables, then toggle the clicked Id
		$(".selectable").removeClass("selected");
		$(toggleId).toggleClass("selected");
	}
	//	If not a single pick selection, check whether the id is selectable
	else if (toggleId.length > 0 && removeId.length > 0 && $(toggleId).hasClass("selectable")) {
		$(toggleId).toggleClass("selected");
  		$(removeId).removeClass("selected");
  		event.preventDefault();
	}
	
  	return false;
});
