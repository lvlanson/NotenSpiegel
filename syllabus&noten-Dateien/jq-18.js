/*------------------------------------------------------------------------------------------
2010-2017 Falk Neuner
/*------------------------------------------------------------------------------------------
Usage: JS-Code für synamischen Laden (JQ)
Model: JS-lib
/*------------------------------------------------------------------------------------------
 * 16.4 Einführung jqContextValues, arge-Verwendung
 * 17.3 jqRequestMapper, JSONP eliminiert
------------------------------------------------------------------------------------------*/
// TODO sind alle ohne JQ > nach js verlegen
	if (typeof jqRequestValues == "undefined")
	{
		var selectedNodeId = "";
		var selectedPropId = "";
		var jqSelectedNodeId = "";

		// WebForm
//TODO vars umbenennen in jqRequestValues, dazu TYPO3-Extension aktualisieren
		// Reload-Daten, perspekt. wird nur noch temp values oder JSON-Var übermittelt, > jqReloadCell
		// damit keine Quereffekte eintreten können
		// jqRequestValues wird runtergefahren, sollen auch nur durch Controls verwendet werdeb 
		var jqRequestValues = new Array(); 		// Formularwerte WebForm, SearchForm
		var jqRequestMapper = new Array();		// 17.3 Bind-Pointer bei FormObjectAC, zeigt auf zu bindenen Value (für Suchformulare notwendig die kein Post-verhalten haben)
		var jqControlValues = new Array();		// Formularfelder, die bei JQ gesendet werden
		var jqControlTitles = new Array();		// Texteingabefelder bei FormObjectAC
		
		// Zustandsmanagement CMS:7479 (durch canvas-id indiziert)
//TODO Bereinigung der Registrierungen
		var jqCanvasPrefix = new Array();		// CmsCell::prefix Differenzierung von Cookies bei CMS-verwalteten SysList/SysTree
		var jqCanvasArgument = new Array(); 	// clientseitiges argc (Ermöglichung mehrerer Objekte)
		var jqCanvasValues = new Array(); 		// expandierte objektdaten (zur Ermöglichung mehrerer Objekte)
		var jqCanvasCookie = new Array(); 		// komprimierte Cookie-Daten, Wegfall jqCanvasPrefix (geplant 18.1)
						
		// Kontextmanagement CMS:7479
		var jqContextValues = new Object(); 	//aktueller Anzeigekontext, Zeit, Target, Seite
		var jqContextCallbacks = new Array(); 	// 17.2 Callbacks Wechel Step, Page, TODO Time,Page ... Index ist canvas, CMS:7479

		// hiermit können sich Extensions finden und kommunizieren
		// gehört aktuell nicht zum Kernsystem
		// nur für FOM/TYPO3-Integration: kooperierende Extensions in custom/hsmw/extensions
		var jqRegisterExtension = new Array();	// Extension
		var jqRegisterCanvas = new Array();
		var jqRegisterState = new Array();
		
		var jqInputAutoSelectCanvas = undefined;	// Auto-Focus
		
		var jqModalDialog = null;				// jqModalMessage()
		
		var jqSubmitBusy = 0;					// Schutz gegen Doppelclick

	}
	
	/**
	 * URL JQ-Dispatcher
	 * @returns {String}
	 */
	function jqUrl()
	{
		var dir = document.location.href.split('/')[3];
		if (document.location.href.split('/').length < 5) dir = "nsoft";
		return '/' + dir + '/jquery.asp';
	}
	
	/**
	 * 
	 * @param html
	 * @returns
	 */
	function jqGetJsonData(html)
	{
		var data;
		var tilde = String.fromCharCode(126);
		var indexOfJson = html.indexOf('<!-- ' + tilde);
		var endOfJson = html.indexOf(tilde + ' -->');
		if (indexOfJson >= 0 && endOfJson > indexOfJson)
		{					
			try 
			{
				var json = html.substring(indexOfJson + 6,endOfJson);
				return JSON.parse(json);
			}
			catch (err) 
			{
			}
		}
	}
	
// TODO jqReloadCell 
	/**
	 * Extensioninhalt laden
	 * Anzeige topmessages
	 * @param canvas Element Container
	 * @param argc Klassenname + Configuration
	 * @param arge Extension Nutzerinteraktion/Operationsdaten: Feld oder String
	 * @param argu NICHT VERWENDET/Reserve
	 * @param vars ersetzt jqRequestValues
	 * @param async asynchron (Standard)
	 * @param successFct Callback
	 */
	function jqReloadVars(canvas,argc,arge,argu,vars,async,successFct) 
	{
		var url;
		var extension;
		if (argc.split('?')[1] == undefined)
		{
			url = jqUrl();
			extension = argc.split(':')[0];
		}
		else
		{
			url = argc.split('?')[0];
			extension = argc.split('?')[1].split(':')[0];
		}
		
		var data = {
			extension : extension,
			canvas : canvas,
			argc : argc
		}
		if (vars == undefined && jqRequestValues != undefined) vars = jqRequestValues; 	// jqRequestValues soll hier wegfallen
		if (vars != undefined) for (var key in vars) data[key] = vars[key];				// vars kann eventuell wegfallen
		
//TODO var arge =  new Array(); verwenden. oder var arge = [...
//var arge = {... geht nicht, new Object() geht auch nicht
		// arge (Operationsdaten)
		if ($.isArray(arge)) for (var key in arge) data[key] = arge[key];
		else data['arge'] = arge;
		
		// canvas-Status, wenige Verwendungen
		if (jqCanvasValues[canvas] && !data[canvas]) data[canvas] = JSON.stringify(jqCanvasValues[canvas]);

		// context an Server senden
		data['context'] = JSON.stringify(jqContextValues);
		
		// 17.2 Medienportal (SysList-Links)
		if (typeof pagePortal != 'undefined') data['page_portal'] = pagePortal;
		
		//JqGridLoading geht nur bei async
		if (async == undefined) async = true;
		$('#'+canvas.replace('_table','')).addClass('JqGridLoading');
		
		// GET ermöglicht besseres Debugging, da URL aus Entwicklertool direkt getestet werden kann
		var options = {
			type: 'POST',
			dataType: 'html',
			url: url,
			cache: false,
			async: async,
			data: data,
    		error: function(xhr, textStatus, errorThrown) 
			{
    			jqSubmitBusy = 0;
    			$('#'+canvas.replace('_table','')).removeClass('JqGridLoading');

    			// Audit
    			var dump = url + ' ';
    			for (var key in data) dump += key + ':' + data[key] + ', ';
    			jqAudit('error ' + xhr.status + ' ' + textStatus,dump,'jqReloadVars');
    			
    			// Alert
    			if (!errorThrown) errorThrown = '';
    			if (xhr.status == 0) errorThrown += ' (Netzwerkfehler)'; 
    			if (xhr.status > 0) errorThrown += ' (Fehler ' + xhr.status + ')'; 
    			if (extension != 'SysTickerCell' && extension != 'QdeTermineAlarm') alert('Es ist ein Fehler in jqReloadVars von ' + extension + ' aufgetreten: ' + errorThrown);
			},
			success: function(html, textStatus) 
			{
    			jqSubmitBusy = 0;
				// eventuell PHP-Fehler
				if (!html)
				{
	    			var dump = url + ' ';
	    			for (var key in data) dump += key + ':' + data[key] + ', ';
					jqAudit('html is empty',dump,'jqReloadVars');
				}
				
				// 17.1 JSON-Container (CmsCell)
				var data;
				var tilde = String.fromCharCode(126);
				var indexOfJson = html.indexOf('<!-- ' + tilde);
				var endOfJson = html.indexOf(tilde + ' -->');
				if (indexOfJson >= 0 && endOfJson > indexOfJson)
				{					
					try 
					{
						var json = html.substring(indexOfJson + 6,endOfJson);
						data = JSON.parse(json);
					}
					catch (err) 
					{
						jqAudit(err.message,json,'jqReloadVars');
					}
				}
				
				// 17.1 redirect ausführen (SysList-Ops)
				if (data && data['redirect']) 
				{
					pageLoad(data['redirect']);
					return;
				}
				if (data && data['redirect']) 
				{
					pageLoad(data['redirect']);
					return;
				}
				
				$('#'+canvas.replace('_table','')).removeClass('JqGridLoading');
				$('#'+canvas).empty();
				$('#'+canvas).append(html);
				
				// 17.2 neues ORL-Exportverfahren, EmmiFPList
				if (data && data['onload'])
				{
					jsLog('onload: ' + data['onload'],'ReloadAppl');
					eval(data['onload']);
				}
				
				// jqInputAutoSelectCanvas
				// GridEditing HisDvsRecordGrid, ATC
				if (extension != 'SysTickerCell' && extension != 'QdeTermineAlarm') if (jqInputAutoSelectCanvas != undefined && document.getElementById(jqInputAutoSelectCanvas)) 
				{
					if (document.getElementById(jqInputAutoSelectCanvas).tagName == 'INPUT')
					{
						document.getElementById(jqInputAutoSelectCanvas).focus();
						document.getElementById(jqInputAutoSelectCanvas).select();
					}
					else if (document.getElementById(jqInputAutoSelectCanvas).tagName == 'TEXTAREA')
					{
						document.getElementById(jqInputAutoSelectCanvas).focus();
						document.getElementById(jqInputAutoSelectCanvas).select();
					}
					else if (document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('select').length > 0)
					{
						document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('select')[0].focus();
					}
					else if (document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('input').length > 0)
					{
						document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('input')[0].focus();
						document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('input')[0].select();
					}
					else if (document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('textarea').length > 0)
					{
						document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('textarea')[0].focus();
						document.getElementById(jqInputAutoSelectCanvas).getElementsByTagName('textarea')[0].select();
					}
				}
				
				// Spaltenanpassung herstellen
				if (typeof SysTree_cb_form == "object" && SysTree_cb_form[canvas] != undefined) SysTree_Loaded(canvas);
				if (typeof SysList_columns == "object" && SysList_columns[canvas.replace('_table','')] != undefined) SysList_Loaded(canvas.replace('_table',''));
				if (successFct!= undefined) successFct();

				// 16.3 Message-Rücktransport von SysList::RenderMessage (marea/$page_listMessageArea)
				if (data && data['messages'])
				{
					$('#comp_topmessages').empty();
					$('#comp_topmessages').append(data['messages']);
				}
				
				// Rückgabewert in HTML-Kommentar, wo verwendet ???
				// dürfte bei async nicht funktionieren
				if (!data && html[0] == '<' && html[1] == '!')
				{
					return html.split(' ')[1];
				}
			}
		}
		$.ajax(options);	
	}
	
	/**
	 * 18.2 Zell neu laden
	 * jqReloadCell soll jqReloadVars ablösen
	 * @param canvas
	 * @param argc
	 * @param arge REQUEST-Daten
	 * @param successFct
	 */
	function jqReloadCell(canvas,argc,arge,successFct) 
	{
		jqReloadVars(canvas,argc,arge,undefined,undefined,undefined,successFct) 
	}
	
// TODO	läuft aus, jqReloadCell arge verwenden
	/**
	 * 16.4 arge, jqCanvasValues oder jsonValues soll anstelle jqRequestValues verwendet werden, damit nur kontrollierte Daten übertragen werden (CmsFavorites)
	 * @param canvas
	 * @param argc
	 * @param jsonValues läuft aus, jqReloadCell arge verwenden
	 */
	function jqReload(canvas,argc,jsonValues) 
	{
		if (argc == undefined) argc = jqCanvasArgument[canvas.replace('_table','')];
		if (jsonValues != undefined)
		{
			var vars = {}
			vars[canvas] = jsonValues;
			return jqReloadVars(canvas,argc,null,null,vars);
		}
		return jqReloadVars(canvas,argc);
	}
	
	/**
	 * 17.2 jqContextCallbacks ausführen CMS:7479
	 * @param key
	 * @param value
	 * @param noContext 19.1 nicht in jqContextValues speichern (wird als allgemeines Event verwendet SplMove)
	 */
	function jqContextCallback(key,value,noContext) 
	{
		//jsLog('key:'+ key +' length:' + jqContextCallbacks.length,'jqContextCallback');
		if (!noContext) jqContextValues[key] = value;
		for (var i in jqContextCallbacks) 
		{
			//jsLog('index:'+ i + ' key:'+key + ' value:'+ value ,'jqContextCallback');
			if (typeof jqContextCallbacks[i] == "function")	jqContextCallbacks[i](key,value);
			//else jsLog('index:'+ i +' invalid','jqContextCallback'); // Bereinigung Feld ???
		}	
	}
	
	/**
	 * 17.2 Download ausführen mit iframe, 
	 * url muss Content-disposition/Content-type setzen 
	 * https://stackoverflow.com/questions/3749231/download-file-using-javascript-jquery
	 * https://stackoverflow.com/questions/7027799/access-elements-of-parent-window-from-iframe
	 * Fehlerhandling muss durch url erfolgen: Ausgabe Javascript-alert()/jqModalMessage() anstelle Content-disposition (ComInvoicesList)
	 * @param url
	 */
	function jqDownload(url) 
	{
		var idown = document.getElementById('jqDownload_frame');
		if (idown) {
			idown.src = url;
		} else {
			$('<iframe>', { id:'jqDownload_frame', src:url }).hide().appendTo('body');
		}
	}

	/**
	 * 17.3 Render direkt im Browser ausführen
	 * @param url
	 * @param frame 1|blank|download|null
	 */
	function jqRedirect(url,frame) 
	{
		if (frame == "download") jqDownload(url);
		else if (frame) window.open(url,'_blank');
		else window.location = url;
	}
	
	/**
	 * auf JQ-Dispatcher umleiten, 
	 * verwendet für Reporting (pdf,csv,xml)
	 * @param argc
	 * @param vars
	 * @param frame 1|blank|download|null
	 */
	function jqRedirectVars(argc,vars,frame) 
	{
		var url;
		var extension;
		if (argc.split('?')[1] == undefined)
		{
			url = jqUrl();
			extension = argc.split(':')[0];
		}
		else
		{
			url = argc.split('?')[0];
			extension = argc.split('?')[1].split(':')[0];
		}
		url = url + '?extension=' + extension;

		if (jqRequestValues != undefined) 
		{
			for(var key in jqRequestValues) url += '&'+key+'='+encodeURIComponent(jqRequestValues[key]);
		}
		if (vars != undefined) 
		{
			for(var key in vars) url += '&'+key+'='+encodeURIComponent(vars[key]);
		}
		jqRedirect(url,frame);
	}
	
/*--------------------------------------------------------
Registrierung Events (späte Bindung)
aktuell werden nur die Extensions registriert.
Extension können somit andere Extensions finden, mit denen sie kooperieren finden und ansteuern.
Provisorium
NICHT VERWENDET
--------------------------------------------------------*/
	function jqRegisterCell(extension,canvas,argc)
    {
		jqRegisterExtension[jqRegisterState.length] = extension;
		jqRegisterCanvas[jqRegisterState.length] = canvas;
		jqRegisterState[jqRegisterState.length] = argc;
	}	
	
	// Prefix ermitteln (altes Verfahren zur Kennzeichnung von canvas seinen jqRequestValues)
	// Prefix wird nur für CMS-Zellen verwendet (die aus DB stammen, Id)
	function jqGetPrefix(canvas) 
    {
		var prefix = jqCanvasPrefix[canvas.replace('_table','')];
		if (prefix == undefined) prefix = '';
		return prefix;
    }

	// absoluten Pfad ermitteln
	// document.location.pathname ???
	function jqGetPath() 
    {
		var i = 0;
		if (window.location.href.substr(0,7) == 'http://') i = 7 + window.location.href.substr(7).indexOf('/');
		if (window.location.href.substr(0,8) == 'https://') i = 8 + window.location.href.substr(8).indexOf('/');
		var path = window.location.href.substr(i);
		if (path.indexOf('?') > 0)
		{
			path = path.substr(0,path.indexOf('?'));
		}
		return path;
	}
	
	/**
	 * Cookie für canvas setzen (jqCanvasPrefix berücksichtigen)
	 * @param canvas
	 * @param key
	 * @param val
	 * @param expire
	 * @param path
	 */
	function jqSetCookie(canvas,key,val,expire,path) 
    {
		var prefix = jqGetPrefix(canvas);
		if (expire == undefined)
		{
			expire = 30; //Tage
		}
		if (path == undefined)
		{
			path = jqGetPath();
		}
		var cookie = prefix + 'nsoft_' + key;
		jsSetCookie(cookie,val,expire,path);
	}

	/**
	 * Input lesen
	 * Erkennung checkbox mittels className, am besten über attributes[].nodeName='type' ermitteln
	 * Erkennung radio mittels length (node ist ein Feld aus input)
	 * ansonsten input oder select
	 * @param node
	 * @returns
	 */
	function jqGetControlValue(node) 
	{
  		if (node.className == 'checkbox' || node.getAttribute('type') == "checkbox")
  		{
  			if (node.checked) return 1;
  			if (!node.checked) return 0;
  		}
 		if (node.length != undefined && node.length > 0 && node[0].tagName == 'INPUT')
		{
			for (var i = 0; i < node.length; i++) 
			{
				if (node[i].checked) return node[i].value;
			}
		}
		else
			return node.value;
	}

	/**
	 * Absenden SearchForm
	 * jqControlValues lesen
	 * Cookies (mit UTF8 encode) mit jqControlValues/jqControlTitles setzen
	 * jqRequestValues setzen
	 * Reload canvas ausführen
	 * nur von SysList/SysTree verwendet
	 * @param canvas
	 * @param argc
	 * @param expire
	 * @param path
	 */
	function jqSubmit(canvas,argc,expire,path) 
    {
		// fängt Doppelclick ab
		if (jqSubmitBusy) 
		{
			jsLog('jqSubmitBusy','jqSubmit');
			//jqAudit('error jqSubmitBusy ' + canvas,null,'jqSubmit');
			return;
		}
		jqSubmitBusy = 1; 
		
		// jqControlValues
		for (var i = 0; i < jqControlValues.length; i++) 
	   	{
	   		var node = document.getElementById(jqControlValues[i]);
	   		if (!node)
	   		{
	   			jsLog(jqControlValues[i]+': no element found','jqSubmit/jqControlValues');
	   			continue;
	   		}
	   		var value = jqGetControlValue(node);
	   		jqSetCookie(canvas,jqControlValues[i],jsUtf8Encode(value),expire,path);
	   		if (jqRequestMapper[jqControlValues[i]]) jqRequestValues[jqRequestMapper[jqControlValues[i]]] = value;
	   		else jqRequestValues[jqControlValues[i]] = value;
			jsLog(jqControlValues[i]+ ' value: '+value,'jqSubmit/jqControlValues');
		}
		
	   	// jqControlTitles AC-Controls
		for (var i = 0; i < jqControlTitles.length; i++) 
		{
	   		var node = document.form1[jqControlTitles[i]];
	   		if (!node)
	   		{
	   			jsLog(jqControlTitles[i]+': no element found','jqSubmit/jqControlTitles');
	   			continue;
	   		}
	   		var value = node.value
			jqSetCookie(canvas,jqControlTitles[i],jsUtf8Encode(value),expire,path);
	   		if (jqRequestMapper[jqControlTitles[i]]) jqRequestValues[jqRequestMapper[jqControlTitles[i]]] = value;
			jsLog(jqControlTitles[i]+': '+value,'jqSubmit/jqControlTitles');
		}
		jqReloadVars(canvas,argc);
		//TODO callback jqSubmitBusy = 0;, damit jqSubmitBusy nur hier bekannt
		//TODO jqSubmitBusy müsste je canvas verwaltet werden (array)
	}
	
/*--------------------------------------------------------
Rücksetzen (SearchForm)
TODO Defaultwerte woher nehmen? müssten im Client hinterlegt werden (JS) wenn DefaultValue
TODO Behandlung spezieller Controls evt. über reg. Funktionen (DateControl)
--------------------------------------------------------*/
	function jqResetForm(canvas,argc,expire,path) 
    {
		for (var i = 0; i < jqControlValues.length; i++) 
	   	{
	   		///value = document.form1[jqControlValues[i]].value;
			//console.log('nodeName:'+document.form1[jqControlValues[i]].nodeName);
	   		if (document.form1[jqControlValues[i]].nodeName == "SELECT") 
	   		{
	   			var first = document.form1[jqControlValues[i]].getElementsByTagName('option')[0].value;
	   			document.form1[jqControlValues[i]].value = first;
	   			jqSetCookie(canvas,jqControlValues[i],'',expire,path);
				jqRequestValues[jqControlValues[i]] = first;
	   		}
	   		else
	   		{
	   			document.form1[jqControlValues[i]].value = '';
	   		//if (document.form1[jqControlValues[i]].className == 'checkbox' && document.form1[jqControlValues[i]].checked) value = 1;
	   		//if (document.form1[jqControlValues[i]].className == 'checkbox' && !document.form1[jqControlValues[i]].checked) value = 0;
		   		jqSetCookie(canvas,jqControlValues[i],'',expire,path);
				jqRequestValues[jqControlValues[i]] = undefined;
	   		}
		}
		for (var i = 0; i < jqControlTitles.length; i++) 
		{
			jqSetCookie(canvas,jqControlTitles[i],'',expire,path);
		}
		jqReloadVars(canvas,argc);
	}

	/**
	 * Ereignis an SysAuditService senden
	 * @param info
	 * @param data
	 * @param op
	 */
	function jqAudit(info,data,op) 
	{
		var url = jqUrl();
		var data = {
			'service' : 'sys/services/SysAuditService',
			'info' : info,
			'data' : data,
			'type' : 20,
			'op' : op
		}
		var options = {
			type: 'POST',
			url: url,
			async: true,
			data: data,
			dataType: 'html',
			error: function(xhr, textStatus, errorThrown) 
			{
				//alert('Es ist ein Fehler in jqAudit aufgetreten! ' + errorThrown);
			},
			success: function(html, textStatus) 
			{
				//jsLog(html,'jqAudit');
			}
		}
		$.ajax(options);		
	}
	
	//TODO callback verwenden (siehe SysCell)
	//TODO values verwenden, jqRequestValues weg (siehe CmsLinkEdit)
	/**
	 * Änderungsoperation ohne html-Rücktransport (Mini-Forms)
	 * @param argc
	 * @param vars
	 * @param async soll entfallen (immer async + callback)
	 * @param callback soll immer verwendet werden
	 * @returns
	 */
	function jqExecute(argc,vars,async,callback) 
	{
		var url;
		var extension;
		if (argc.split('?')[1] == undefined)
		{
			url = 		jqUrl();
			extension = argc.split(':')[0];
		}
		else
		{
			url = 		argc.split('?')[0];
			extension = argc.split('?')[1].split(':')[0];
		}
		
		var data = {
			'extension' : extension,
			'argc' 		: argc
		}

		if (vars == undefined && jqRequestValues != undefined) vars = jqRequestValues;//??? soll verschwinden
		if (vars != undefined) for (var key in vars) data[key] = vars[key];
		
		//Loading-Animation geht nut bei async
		if (async == undefined) async = false;
		var result = undefined;
		
		data['context'] = JSON.stringify(jqContextValues);
		var options = {
			type: 'POST',
			url: url,
			//cache: true,
			async: async,
			data: data,
			dataType: 'html',
			error: function(xhr, textStatus, errorThrown) 
			{
    			errorThrown += ' (Fehler ' + xhr.status + ')'; 
				if (typeof callback == 'function') callback('Es ist ein Fehler in jqExecute aufgetreten: ' + errorThrown);
				else alert('Es ist ein Fehler in jqExecute aufgetreten: ' + errorThrown);
			},
			success: function(html, textStatus) 
			{
				if (typeof callback == 'function') callback(html);
				else result = html;
			}
		}
		$.ajax(options);
		return result;
	}
	
/*--------------------------------------------------------
jqReloadPlugin für Toggle-Zellen und Aktualisieren abhängiger Zellen (Datumswechsel)
gehört zu CmsCell
--------------------------------------------------------*/
	function jqReloadPlugin(canvas,argc,md5) 
	{
		if ($('#header_'+canvas).hasClass('CmsCellHidden')) return;
		jsLog(canvas + ' ' + argc,'jqReloadPlugin');

		if (argc.split('?')[1] == undefined)
		{
			url = jqUrl();
			extension = argc.split(':')[0];
		}
		else
		{
			url = argc.split('?')[0];
			extension = argc.split('?')[1].split(':')[0];
		}
		
		$('#'+canvas).addClass('JqGridLoading');
		var data = {
			'extension' : extension,
			'canvas'	: canvas,
			'argc'		: argc,
			'nocache' 	: new Date().getTime()
		}
		
		// canvas-Status
		if (jqCanvasValues[canvas]) data[canvas] = JSON.stringify(jqCanvasValues[canvas]);

		// context
		data['context'] = JSON.stringify(jqContextValues);
		
		//jsLog('context: ' + data['context'],'jqReloadPlugin');
		data['md5'] = md5;
		var options = {
			type: 'GET',
			url: url,
			data: data,
			async: true,
			dataType: 'html',
			error: function(xhr, textStatus, errorThrown) 
			{
				$('#'+canvas).removeClass('JqGridLoading');
				$('#'+canvas).empty();
				$('#'+canvas).append('Es ist ein Fehler in jqReloadPlugin von Extension '+extension+' aufgetreten: ' + errorThrown);
			},
			success: function(html, textStatus) 
			{
				$('#'+canvas).removeClass('JqGridLoading');
				$('#'+canvas).empty();
				$('#'+canvas).append(html);
				
				// 16.4.1 json/replace anzeigen (Cell-Header)
				if (html.indexOf('<!-- ~') != -1)
				{
					var json = html.split('~')[1];
					if (json)
					{
						data = JSON.parse(json);
						if (data && data['replace'])
						{
							for (var key in data['replace']) 
							{
								var jqpath = key; 
								if (key.substring(0,1) != '#') jqpath = '#' + key; 
								jsLog(jqpath,'jqReloadPlugin');//
								$(jqpath).empty();
								$(jqpath).append(data['replace'][key]);
							}
						}
					}
				}
			}
		}
		$.ajax(options);
	}
	
	/**
	 * Content von URL in Node laden (zumeist von Controls verwendet)
	 * @param node_id
	 * @param url
	 * @param vars
	 * @param async
	 */
	function jqReloadFrom(node_id,url,vars,async) 
	{
		$('#'+node_id).addClass('JqGridLoading');
		if (data == undefined)
		{
			var data = {
				'nocache' : new Date().getTime()
			}
		}
		if (vars != undefined)
		{
			for (var key in vars)
				data[key] = vars[key];
		}
		if (async == undefined) async = false;
		data['context'] = JSON.stringify(jqContextValues);
		var options = {
			type: 'GET',
			url: url,
			data: data,
			async: async,
			dataType: 'html',
			error: function(xhr, textStatus, errorThrown) 
			{
				$('#'+node_id).removeClass('JqGridLoading');
				$('#'+node_id).empty();
				$('#'+node_id).append('Es ist ein Fehler in der Funktion in jqReloadFrom aufgetreten: ' + errorThrown);
			},
			success: function(html, textStatus) 
			{
				$('#'+node_id).removeClass('JqGridLoading');
				$('#'+node_id).empty();
				$('#'+node_id).append(html);
			}
		}
		$.ajax(options);
	}

//TODO synchrones Laden soll ersetzt werden durch Callbacks, siehe jqExecute
	/**
	 * Content synchron von URL laden (Trees)
	 * Fehler bei timergesteuerten Aufrufen verbergen (QdeTermineAlarm) Computer-Ruhe, Netzwerkproblem
	 * @param url
	 * @param vars
	 * @returns
	 */
	function jqLoadFrom(url,vars) 
	{
		var result;
		if (data == undefined)
		{
			var data = {
				'nocache' : new Date().getTime()
			}
		}
		if (vars != undefined)
		{
			for (var key in vars)
				data[key] = vars[key];
		}
		data['context'] = JSON.stringify(jqContextValues);
		var options = {
			type: 'GET',
			url: url,
			data: data,
			async: false,
			dataType: 'html',
			error: function(xhr, textStatus, errorThrown) 
			{
				var text;
				for (var key in data) text += key+':'+data[key]+', ';
				if (url.indexOf('QdeTermineAlarm') == -1) alert('Es ist ein Fehler in jqLoadFrom von ' + url + ' aufgetreten! ' + errorThrown + text);
			},
			success: function(html, textStatus) 
			{
				result = html;
			}
		}
		$.ajax(options);
		return result;
	}

/*--------------------------------------------------------
Expand/Collapse
--------------------------------------------------------*/
	function jqAddClass(node_id,className) 
	{
		$('#'+node_id).addClass(className);
	}
	function jqRemoveClass(node_id,className) 
	{
		$('#'+node_id).removeClass(className);
	}
	function jqHideNode(node_id) 
	{
		$('#'+node_id).addClass('NodeHidden');
	}
	function jqShowNode(node_id) 
	{
		$('#'+node_id).removeClass('NodeHidden');
	}

	function jqSelectNode(node_id) 
	{
		// Links usw. sollen ohne Markierungswechsel ausgeführt werden
		if (window.event && window.event.target && window.event.target.tagName == 'A') return;
		if (window.event && window.event.target && window.event.target.tagName == 'IMG') return;
		if (window.event && window.event.target && window.event.target.tagName == 'SPAN') return;
		
		/* toogle nicht immer gewollt ??? */
		if (jqSelectedNodeId != "" && jqSelectedNodeId == node_id)
		{
			$('#'+jqSelectedNodeId).removeClass('JqNodeFocus');
			$('#'+jqSelectedNodeId).removeClass('JqNodeFocusAlt');
			jqSelectedNodeId = undefined;
			jsStopPropagation(); // geschachtelte Nodes möglich
			return;
		}
		if (jqSelectedNodeId != "" && document.getElementById(jqSelectedNodeId) != undefined) 
		{
			$('#'+jqSelectedNodeId).removeClass('JqNodeFocus');
			$('#'+jqSelectedNodeId).removeClass('JqNodeFocusAlt');
		}
		jqSelectedNodeId = node_id;
		$('#'+node_id).addClass('JqNodeFocus');
		if ($('#'+node_id).hasClass('AppGridAlt')) $('#'+node_id).addClass('JqNodeFocusAlt');
		jsStopPropagation();
	}
	function jqUnselectNode() 
	{
		if (jqSelectedNodeId != "" && document.getElementById(jqSelectedNodeId) != undefined) 
		{
			$('#'+jqSelectedNodeId).removeClass('JqNodeFocus');
			$('#'+jqSelectedNodeId).removeClass('JqNodeFocusAlt');
		}
	}
		
	/* 
	 * http://jqueryui.com/dialog/#modal-message dialog
	 * http://www.w3schools.com/howto/howto_css_modals.asp
	 * siehe Template-Vorkehrung modal-message: /php/custom/layouts/nsoft/appl.finish.htm
	 */
	/**
	 * Modal alert/confirm
	 * @param text
	 * @param callback Aktion für Fortfahren
	 * @param cancelCallback Aktion für Abbrechen
	 * @param confirmLabel 18.2
	 * @param cancelLabel 18.2
	 */
	function jqModalMessage(text,confirmCallback,cancelCallback,confirmLabel,cancelLabel) 
	{
		if (!jqModalDialog)
		{
			jqModalDialog = document.getElementById('modal-message');
			if (jqModalDialog) 
			{ 
				window.onclick = function(event) {
				    if (event.target == jqModalDialog) 
				    {
				    	jqModalDialog.style.display = "none";
				    	if (typeof cancelCallback == 'function') cancelCallback();
				    }
				};
			}
		}
		if (jqModalDialog) 
		{
			var dialog = document.getElementById('modal-message-dialog');
			var close = document.getElementById('modal-message-close');
			var cancel = document.getElementById('modal-message-cancel');
			var confirm = document.getElementById('modal-message-confirm');
			
			confirm.innerHTML = 'Fortfahren';
			cancel.innerHTML = 'Abbrechen';
			if (confirmLabel) confirm.innerHTML = confirmLabel;
			if (cancelLabel) cancel.innerHTML = cancelLabel;
			
			// TODO ML Fortfahren, Abbrechen, Schließen
			//if (confirmLabel)
			
			// close
			dialog.onkeydown = function (e) {
				var event = e || window.event;
				if (event.keyCode == 27) 
				{
					jqModalDialog.style.display = "none";
					if (typeof cancelCallback == 'function') cancelCallback();
					return false;
				}
				if (event.keyCode == 13) 
				{
					jqModalDialog.style.display = "none";
					return false;
				}
				return true;
			};
			close.onclick = function() {
				jqModalDialog.style.display = "none";
				if (typeof cancelCallback == 'function') cancelCallback();
			}
			
			// Cancel
			cancel.onclick = function() {
				jqModalDialog.style.display = "none";
				if (typeof cancelCallback == 'function') cancelCallback();
			}
			
			// reset buttons (AJAX-load)
			close.style.display = "inline";
			cancel.style.display = "none";
			confirm.style.display = "none";

			// confirmCallback
			if (typeof confirmCallback == 'function') 
			{
				close.style.display = "none";
				cancel.style.display = "inline";
				confirm.style.display = "inline";
				confirm.onclick = function() {
					jqModalDialog.style.display = "none";
					confirmCallback();
				};
				dialog.onkeydown = function (e) {
					var event = e || window.event;
					if (event.keyCode == 27) 
					{
						jqModalDialog.style.display = "none";
						if (typeof cancelCallback == 'function') cancelCallback();
						return false;
					}
					if (event.keyCode == 13) 
					{
						jqModalDialog.style.display = "none";
						confirmCallback();
						return false;
					}
					return true;
				};
			}
			
			// text
			var content = document.getElementById('modal-message-text');
			content.innerHTML = text;
			jqModalDialog.style.display = "block";
			
			// wegen Esc/Enter tabindex=-1 erforderlich
			dialog.focus();
		}
		// Rückfall auf alert() confirm() wenn modal-message im Template nicht gefunden
		else 
		{
			if (typeof confirmCallback == 'function') 
			{
				if (confirm(text)) confirmCallback();
			}
			else 
			{
				alert(text);
			}
		}
	}
	
	/**
	 * Link mit Bestätigung
	 * @param text
	 * @param href
	 */
	function jqConfirmGoto(text,href)
	{
		jqModalMessage(text,function() {
			pageLoad(href);
		});
	}
