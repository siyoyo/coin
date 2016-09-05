/*
* Reference:
* http://stackoverflow.com/questions/18442167/how-to-read-xml-file-using-javascript-in-chrome-only
* https://www.youtube.com/watch?v=61MFRaZ_FhE
* https://blog.udemy.com/javascript-page-refresh/
*/

function timedRefresh() {
	setTimeout("location.reload(true)", 10000);
	loadXMLDoc();
}

function loadXMLDoc() {

	var ajaxObject;
    var xmlDoc;

	if (window.XMLHttpRequest) {
		ajaxObject = new XMLHttpRequest();
	} else {
		ajaxObject = new ActiveXObject("Microsoft.XMLHTTP");
	}

	ajaxObject.open("GET", "blockchain.xml", false);
	ajaxObject.send();
	xmlDoc = ajaxObject.responseXML;

	writeBlocks(xmlDoc);
	
}

function writeBlocks(xmlDoc) {

	var blocks = xmlDoc.getElementsByTagName("block");
	
	// Write header
	document.write('<!DOCTYPE html><html><head>');
	document.write('<script src="scripts.js"></script>');
	document.write('<link rel="stylesheet" type="text/css" href="styles.css">');
	document.write('</head>');

	// Write body
	document.write('<body>');
	document.write('<img src="../gui/logo.jpg" alt="HTML5 Icon"><h1>Block Explorer</h1>');
	document.write('<div class="body">');

	// Blocks
	for (i = blocks.length - 1; i >= 0; i--) {

		document.write('<h3>Block ');
		var height = blocks[i].getElementsByTagName("height")[0].childNodes[0].nodeValue;
		document.write(height);
		document.write('</h3>');
		
		// Header
		document.write('<div class="block"><div class="table"><table id="hd"><tr><td id="title">Header hash</td><td id="content">');
		var pow = blocks[i].getElementsByTagName("pow")[0].childNodes[0].nodeValue;
		document.write(pow);
		document.write('</td></tr>');
		
		document.write('<tr><td id="title">Previous hash</td><td id="content">');
		var previousPoW = blocks[i].getElementsByTagName("previousPoW")[0].childNodes[0].nodeValue;
		document.write(previousPoW);
		document.write('</td></tr>');

		document.write('<tr><td id="title">Merkle root</td><td id="content">');
		var merkleRoot = blocks[i].getElementsByTagName("merkleRoot")[0].childNodes[0].nodeValue;
		document.write(merkleRoot);
		document.write('</td></tr></table></div>');

		// Transactions
		var transactions = blocks[i].getElementsByTagName("transaction");
		var transaction;

		for (j = 0; j < transactions.length; j++) {
			
			transaction = transactions[j];

			document.write('<div class="tx"><h4>Transaction ');
			var txID = transaction.getElementsByTagName("txID")[0].childNodes[0].nodeValue;
			document.write(txID);
			document.write('</h4>');

			// Table header
			document.write('<div class="table"><table id="tx"><tr>');
			document.write('<th id="id">Input ID</th>');
			document.write('<th id="address">Input Address</th>');
			document.write('<th id="id">Output ID</th>');
			document.write('<th id="address">Output Address</th>');
			document.write('<th id="amount">Amount</th>');
			document.write('</tr>');
			
			// Inputs
			var inputElements = transaction.getElementsByTagName("input");
			var inputElement;
			var inputs = new Array();
			var input;

			for (k = 0; k <inputElements.length; k++) {
				inputElement = inputElements[k];
				input = {id:inputElement.getElementsByTagName("inputID")[0].childNodes[0].nodeValue, address:inputElement.getElementsByTagName("inputAddress")[0].childNodes[0].nodeValue};
				console.log(input.id);
				console.log(input.address);
				inputs.push(input);
			}

			// Outputs
			var outputElements = transaction.getElementsByTagName("output");
			var outputElement;
			var outputs = new Array();
			var output;

			for (k = 0; k < outputElements.length; k++) {	
				outputElement = outputElements[k];
				output = {id:outputElement.getElementsByTagName("outputID")[0].childNodes[0].nodeValue, address:outputElement.getElementsByTagName("outputAddress")[0].childNodes[0].nodeValue, amount:outputElement.getElementsByTagName("amount")[0].childNodes[0].nodeValue};
				outputs.push(output);
			}

			var rowCount;
			if (inputs.length > outputs.length) rowCount = inputs.length;
			else rowCount = outputs.length;

			// Table body
			for (m = 0; m < rowCount; m++) {
				
				document.write('<tr>');

				// Input ID
				document.write('<td id="id">');
				if (inputs.length != 0) document.write(inputs[m].id);
				document.write('</td>');

				// Input Address
				document.write('<td id="address">');
				if (inputs.length != 0) document.write(inputs[m].address);
				document.write('</td>');

				// Output ID
				document.write('<td id="id">');
				if (outputs.length != 0) document.write(outputs[m].id);
				document.write('</td>');

				// Output Address
				document.write('<td id="address">');
				if (outputs.length != 0) document.write(outputs[m].address);
				document.write('</td>');

				// Amount
				document.write('<td id="amount">');
				if (outputs.length != 0) document.write(outputs[m].amount);
				document.write('</td>');

				document.write('</tr>');
				
				document.write('</table></div></div>'); // Close table and transaction
			}
		}
		document.write('</div>'); // Close block
	}
	document.write('</div>');
	document.write('Celebration graphic by <a href="http://www.freepik.com/">Freepik</a> from <a href="http://www.flaticon.com/">Flaticon</a> is licensed under <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a>. Made with <a href="http://logomakr.com" title="Logo Maker">Logo Maker</a>');
	document.write('</body></html>');
}