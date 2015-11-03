/*
 * Gets a string representing the specified duration in milliseconds.
 * 
 * E.g : duration = 20000100, returns "45 min 20 sec 100 ms"
 */
function formatDuration(duration) {
	var type = $.type(duration);
	if (type === "string")
		return duration;

	// Calculate each part of the string
	var days = Math.floor(duration / 86400000); // 1000 * 60 * 60 * 24 = 1 day
	duration %= 8640000;

	var hours = Math.floor(duration / 3600000); // 1000 * 60 *60 = 1 hour
	duration %= 3600000;

	var minutes = Math.floor(duration / 60000); // 1000 * 60 = 1 minute
	duration %= 60000;

	var seconds = Math.floor(duration / 1000); // 1 second
	duration %= 1000;

	// Add non null part.
	var formatArray = [];
	if (days > 0)
		formatArray.push(days + " day(s)");

	if (hours > 0)
		formatArray.push(hours + " hour(s)");

	if (minutes > 0)
		formatArray.push(minutes + " min");

	if (seconds > 0)
		formatArray.push(seconds + " sec");

	if (duration > 0)
		formatArray.push(duration + " ms");

	// Build the string
	return formatArray.join(" ");
}

/*
 * Gets axis label for the specified granularity
 */
function getElapsedTimeLabel(granularity) {
	return "Elapsed Time (granularity: " + formatDuration(granularity) + ")";
}

/*
 * This comparison function evaluates abscissas and sort them. 
 */
function compareByXCoordinate(coordA, coordB){
	return coordB[0] - coord1[0];
}