/*------------------------------------------------------------------------------------------
Copyright 2003-2017 Falk Neuner, nsoft.de
Ausführung, Bearbeitung, Unterlizensierung nur mit ausdrücklicher Erlaubnis des Urhebers
/*------------------------------------------------------------------------------------------
Usage: Seitenwechsel, AJAX-Unterstützung
Model: JS-Lib
/*------------------------------------------------------------------------------------------
 * 17.1
 * 17.3 Bereinigung Events, getScript-fail
 * 18.1 Unload-Checks, 
------------------------------------------------------------------------------------------*/
	/**
	 * Klasse für Evente-Registrierung campus21 Framework
	 * https://www.phpied.com/3-ways-to-define-a-javascript-class/
	 * 17.3 CMS:7153
	 */
	function pageEventClass(target,event,handler,id,info) 
	{
	    this.target = target;				// Event Target
	    this.event = event;					// Event Name
	    this.handler = handler;				// Event Handler Funktion
	    this.id = id;						// Element.id, Bereinigung erfolgt, wenn Element nicht mehr vorhanden, null = Bereinigung immer
	    this.info = info;					// Logging
	    this.options = 0;					// 1 = Handler ausführen
	}
	
	// AJAX-LoadPage, CMS:7281
	var pageId = null;
	var pageSelf = null;
	var pageReferer = null;
	var pagePortal = null;					// 17.2 Medienportal
	var pageInfo = null;					// 17.2 Seiten-Teaser
	var pageTitle = null;					// 17.3 Seitentitel
	var pageContext = null;					// TODO AJAX unterbrechen bei Portalwechsel, pageContext muss durch Template festgelegt werden, $page_context;
	var pageAjax = 0;						// AJAX-Features
	var pageFound = 'home';					// 17.2 Nav-Auflösung beibehalten CsPageApplication (WebTabs, WebForm)
	var pageTop = 0;						// Editor Scroll-Position
	var pageState = 0;						// Ladezustand 0/1/2/3 (window.History)
	var pageFormBusy = 0;					// Upload-Control busy, provisorische Abwehr Doppelklick Prüfen/Speichern
	var pageLoadedResources = new Array(); 	// loaded JS/CSS resources
	var pageRegisterUnload = new Array(); 	// Unloads im jqPage-Container (WebForm, Tools) Ersatz für window.beforeunload	
	var pageRegisterDocClick = new Array(); // 17.3 Verwaltung der $(document).bind('click',...)
	var pageRegisterEvents = new Array();	// 17.3 Event-Bereinigung CMS:7153
	var pageRegisterRemove = new Array();	// 17.3 Popup-Bereinigung CMS:7153

	// 17.1 wenn jquery.history.js aktiv, History events, AJAX-Reload CMS:7281
	//jquery.history.js muss vor page.js eingebunden werden
	if (typeof window.History.pushState == 'function') 
	{
		window.History.log('window.History activated');
		window.History.Adapter.bind(window,'statechange',function() {
			var State = History.getState(); // Note: We are using History.getState() instead of event.state
			if (pageState == 1) 
			{
				pageState = 3; //statechange bei pushState unterbinden
				window.History.log('statechange:', State.data.id, State.title, State.url, pageState);
				var url = State.url;
				url = pageUrlParams(url,'page_found=' + State.data.found);	// 17.3 ursprüngliches pageFound (CsPageApplikation)
				pageLoad(url,'',State.data.id);
			}
		});
		pageState = 1;
	}
	
	/**
	 * Parameter zu url hinzufügen/ersetzen
	 * soll zu js
	 * @param url
	 * @param params
	 * @returns
	 */
	function pageUrlParams(url,params) 
	{
		if (params[0] == '?') params = params.substring(1);
		if (params[0] == '&') params = params.substring(1);

		var help1 = new Array();
		var help2 = new Array();
		var params1 = new Array();
		if (url.indexOf("?") >= 0)
		{
			help1 = url.substring(url.indexOf("?") + 1).split("&");
			url = url.substring(0,url.indexOf("?"));
			for (var i = 0; i < help1.length; i++)			
			{
				help2 = help1[i].split("=");
				params1[help2[0]] = help2[1];
			}
		}
		help1 = params.split("&");
		for (var i = 0; i < help1.length; i++)			
		{
			help2 = help1[i].split("=");
			params1[help2[0]] = help2[1];
		}
		url = url + '?';
		for (var key in params1) url += key+'='+params1[key]+'&';
		return url.substring(0,url.length - 1);
	}
	
	/**
	 * 17.1 mehrere scripts synchron laden, danach wird callback ausgeführt
	 * @param urls Object (assoz. Array) oder Array
	 * @param callback
	 */
	function pageGetScript(urls,callback)
	{
		if (typeof urls != 'object')
		{
			callback();
			return;			
		}

		//Umwandlung Object in Array
		if (!Array.isArray(urls))
		{
			var object = urls;
			urls = [];
			for (var url in object) if (object[url] == 'JS') urls.push(url);
			jsLog('request ' + urls.length,'pageGetScript');
		}
		
		//bereits geladene Scripte aus urls entfernen
		for (var i = urls.length - 1; i >= 0; i--) 
		{
			if (pageLoadedResources[urls[i]])	urls.splice(i,1);
		}
		
		//URL nicht vorhanden?
		var url = urls.shift();
		if (!url) 
		{
			callback();
			return;
		}
		else
		{
			//17.3 fail auswerten, ohne dem wird bei fehler 404 callback nie ausgeführt
			//https://stackoverflow.com/questions/1406537/handling-errors-in-jquery-getscript/9551999
			jsLog('loading ' + url + ' ...','pageGetScript');			
			$.getScript(url).done(function()
			{
				pageGetScript(urls,callback);
			}).fail(function() 
			{
			    jqAudit('getScript fail',url,'pageGetScript');
			    pageGetScript(urls,callback);
			});			
			pageLoadedResources[url] = 'JS';
		}
	}

	/**
	 * 17.1 AJAX-Seitenwechsel (auch Tab-Wechsel)
	 * @param href 		Seitenwechsel (17.1 AJAX-load)
	 * @param referer 	Seitenwechsel (17.1 AJAX-load, wird aber nicht benötigt)
	 * @param id 		Seitenwechsel (oder aktuelle, wenn Navposition beibehalten)
	 * @param tabs 		bei WebTabs $cms_ajaxModes&2, Nav-position nicht geändert, Extension außerhalb jqPage müssen dann nicht gerendert werden
	 * @return ??? wozu verwendet
	 */
	function pageLoad(href,referer,id,tabs) 
	{
		// 17.4 Unload-Checks ausführen (WebForm, MCE, Tools)
		for (var i in pageRegisterUnload) 
		{
			var error = pageRegisterUnload[i]();
			if (error)
			{
				jqModalMessage(error,function() {
					delete pageRegisterUnload[i];
					pageLoad(href,referer,id,tabs);
				});
				return;
			}
		}
		
		// 17.1 AJAX-Modus prüfen
		if (!(pageAjax&4) && !tabs) 
		{			
			window.location.href = href;
			return 1;
		}
		
		// 17.3 Portalwechsel erkennen, AJAX unterbrechen (pageContext wird durch Template festgelegt) 
		// href kann URL sein z.B. pageReturn()
		var locs = document.location.href.split('/');
		var loc = locs[0] + '//' +locs[2];
		if (!tabs && href && pageContext && !jsStartsWith(href,'?') && !jsStartsWith(href,'page.php') && !jsStartsWith(href,pageContext) && !jsStartsWith(href,loc + pageContext)) 
		{			
			window.location.href = href;
			return 1;
		}		

		// Request-Daten
		var request = {
			page_load 	: 'jq',
			page_id 	: id,			
			referer 	: referer
		}
		if (tabs) request['page_found'] = '' + pageFound; //Seitentitel bestimmen ohne Neuauflösung

		// window.beforeunload löschen
		pageAllowUnload();		

		$('#jqPage').addClass('JqPageLoading'); //wird bei Explorerseiten durch SearchForm überdeckt
		var options = {
			type: 'POST',
			dataType: 'html',
			url: href,
			cache: false,
			data: request,
			error: function(xhr, textStatus, errorThrown) 
			{
				$('#jqPage').removeClass('JqPageLoading');
				if (xhr.status > 0) jqAudit('error ' + xhr.status,href,'pageLoad');
				if (xhr.status > 0) jqModalMessage('Es ist ein Fehler (' + xhr.status + ') für '+ href + ' in pageLoad aufgetreten!');
			},
			success: function(html, textStatus) 
			{
				// PHP-Fehler
				if (!html)
				{
					$('#jqPage').removeClass('JqPageLoading');
					jqAudit('html is empty',href,'pageLoad');
					jqModalMessage('Es ist ein Fehler (empty response) in '+ href + ' aufgetreten!');
					return;
				}
				jsLog('success: ' + href,'pageLoad');
				
				// JSON-Container siehe CMS:7281
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
						jsLog('parse err' + json,'pageLoad');
						jqAudit(err.message,json,'pageLoad');
					}
					//html wird bereinigt, da HTML-Kommentare in JSON sonst den html-Teil stört
					if (indexOfJson == 0) html = html.substring(endOfJson + 5);	// CmsLayoutPage Html nach Json 
					else html = html.substring(0,indexOfJson); // CsPageApplication Html vor Json 
				}

				// Seite darf nicht angezeigt werden, $request_return ausführen bei request_goto(), request_confirm(), request_denied(), request_error()
				if (data && data['return']) 
				{
					$('#jqPage').removeClass('JqPageLoading');
					jsLog('return: ' + data['return'],'pageLoad');
					eval(data['return']);
					return;
				}
				
				// rücksetzen, vollständig ???
				pageRegisterUnload = new Array(); 
				jsPageCookies = new Array(); 
				jqRequestValues = new Array();
				jqRequestMapper = new Array();
				jqControlValues = new Array();
				jqControlTitles = new Array();
				jqContextValues = new Object();
				
				// 17.3 Framework click
				$(document).unbind('click'); // autocomplete DocClick löschen
				$(document).bind('click', pageDocClick);

				// 18.1 Bereinigung der Events tiny_mce-Events mousedown, jquery.uploadfile u.a. CMS:7153
				jsLog("pageRegisterEvents " + pageRegisterEvents.length);
				for (var i in pageRegisterEvents)
				{
					pageRegisterEvents[i].target.removeEventListener(pageRegisterEvents[i].event, pageRegisterEvents[i].handler, false);
					jsLog("pageRegisterEvents remove " + pageRegisterEvents[i].info);
				}
				pageRegisterEvents = new Array();
				
				// 18.1 CMS:7153 Popup-Bereinigung, pageRegisterRemove, aktuell nicht benötigt, Extensions sollten es selber machen
			    /*var elem = document.getElementById('menu_ccontent_spellcheckermenu');
			    if (elem) 
			    {
			    	jsLog("menu_ccontent_spellcheckermenu remove");
			    	elem.parentNode.removeChild(elem);
			    }
			    */
				
				// JSON-Container CMS:7281
				pageId = null;
				pageSelf = null;
				pageTitle = null;
				pageReferer = null;
				pageFound = null;
				pageInfo = null;
				if (data && data['id']) 		pageId = data['id'];
				if (data && data['self']) 		pageSelf = data['self'];
				if (data && data['title'])		pageTitle = data['title'];
				if (data && data['referer'])	pageReferer = data['referer'];
				if (data && data['found']) 		pageFound = data['found'];
				if (data && data['info'])		pageInfo = data['info'];
				jsLog('pageId: ' + pageId,'pageLoad');
				jsLog('pageSelf: ' + pageSelf,'pageLoad');			
				jsLog('pageTitle: ' + pageTitle,'pageLoad');
				jsLog('pageReferer: ' + pageReferer,'pageLoad');
				jsLog('pageFound: ' + pageFound,'pageLoad');			

				// 17.1 Seitenkontext JS-seitig wiederherstellen, wird für Extensions/Grids verwendet (SysAuditList)
				// bei Änderung eventuell Events auslösen für Template-Extensions
				if (data && data['context'])
				{
					for (var key in data['context'])
					{
						jqContextValues[key] = data['context'][key];
					}
				}	
				
				// 17.1 lokale Cookies wiederherstellen, die JS-seitig gelesen werden, siehe jsGetCookie
				if (data && data['cookies'])
				{
					for (var key in data['cookies'])
					{
						jsLog('cookies ' + key + ':' + data['cookies'][key],'pageLoad');
						jsPageCookies[key] = data['cookies'][key];
					}
				}	

				// 17.1 CSS Ressourcen dynamisch laden (Clientseite), ersetzt preload (WebTabs)
				if (data && data['resources'])
				{
					for (var key in data['resources']) 
					{
						if (data['resources'][key] == 'CSS') if (!pageLoadedResources[key])
						{
							jsLog('loading ' + key + ' ...','CsPageApplication');
							$("head").append("<link rel='stylesheet' type='text/css' href='" + key + "'>");
							pageLoadedResources[key] = 'CSS';
						}				
					}
				}
				
				// 17.1 Scripte zuerst laden, dann jqPage und Komponenten
				if (data && data['resources']) var resources = data['resources'];
				pageGetScript(resources,function()
				{
					jsLog('pageGetScript ready','pageLoad');

					// reload RenderHead(), Render()
					$('#jqPage').removeClass('JqPageLoading');
					$('#jqPage').empty();
					$('#jqPage').append(html);

					// 17.1 $request_onload ausführen
					if (data && data['onload'])
					{
						jsLog('onload: ' + data['onload'],'pageLoad');
						eval(data['onload']);
					}

					// 17.1 json/classes setzen
					if (data && data['classes'])
					{
						for (var key in data['classes']) 
						{
							var jqpath = key; 
							if (key.substring(0,1) != '#') jqpath = '#' + key; 
							$(jqpath).attr('class',data['classes'][key]);
							jsLog('classes ' + key + ':' + data['classes'][key],'pageLoad');
						}
					}
					
					// 16.4 json/replace empfangen CMS:7297 (layout_title, page_links u.a.)
					if (data && data['replace'])
					{
						for (var key in data['replace']) 
						{
							//jsLog('replace ' + key,'pageLoad');
							var jqpath = key; 
							if (key.substring(0,1) != '#') jqpath = '#' + key; 
							$(jqpath).empty();
							$(jqpath).append(data['replace'][key]);
							jsLog('replace OK ' + key,'pageLoad');
						}
					}

					//TODO tatsächlicher URL nach Umleitung???
					// document.location.pathname wird durch pushState gesetzt // alert(document.location.pathname);
					// Cookies können auf neue location geschrieben, aber nicht gelesen werden
					// jquery.history.js aktiv, Schreiben der History AJAX-Reload CMS:7281
					if (pageState == 1 && data && pageTitle && pageSelf)
					{
						if (typeof window.History.pushState == 'function') 
						{
							pageState = 2;												// unterbindet statechange
							var url = pageSelf.replace('&page_load=jq',''); 			// wegen Reloads im Backend (page.php)
							var state = {
								'state' : url,
								'found' : pageFound,									// aktuelles pageFound (CsPageApplikation)
								'rand' : Math.random(),
								'id' : pageId
							}
							jsLog('state ' + state.state,'pushState');
							window.History.pushState(state, pageTitle, url);
							History.log('pushState:', url, pageTitle, pageId);
						} 
					}
					pageState = 1;														// dynamisch geladen
					
			    	// Extension clientseitigen Seitenwechsel triggern
			    	jqContextCallback('Page',pageId);
					pageCleanup();
				});						
			}
		}
		jsLog('ajax ... ' + href,'pageLoad');
		$.ajax(options);
		return 1;
	}
	
	// 17.2 assoz. Felder bereinigen
	function pageCleanup() 
	{
		var l = 0;
		var d = 0;
		for (var i in jqContextCallbacks) 
		{
			//if (typeof jqContextCallbacks[i] != "function")	
			if (!document.getElementById(i)) 
			{
				jsLog('jqContextCallbacks '+ i +' invalid','pageCleanup'); // Bereinigung Feld ???
				delete jqContextCallbacks[i];
				d++;
			}
			l++;
		}
		jsLog('jqContextCallbacks length:'+ l + ' deleted:' + d ,'pageCleanup');		
	}

	// 17.2 Scrollen, gehört zu page.edit bzw. return
	//https://plainjs.com/javascript/styles/get-and-set-scroll-position-of-an-element-26/
	function pageScroll(top) 
	{
		jsLog('pageScroll ' + top);
		document.body.scrollTop = 0 + top;
		document.documentElement.scrollTop = 0 + top;
	}
	
	// Bestätigung Wikiseite anlegen, funkt, gehört eventuell hier nicht hin (BEE)
	function pageConfirm(info,href)
	{
		jqModalMessage(info,function() {
			pageLoad(href + '&confirm_ok=1');
		});
	}
	function pageError(text)
	{
		jqModalMessage(text,null,null); // wenn im pageConfirm eingebettet, dann close funkt nicht (callback ist definiert)
	}
	
	// Seitenwechsel blockieren bei Änderung in Formularfeldern
	function pageBlockUnload(message)
	{
		jsLog('pageBlockUnload');
		if (!message) message = 'Änderungen wurden nicht gespeichert.';
		pageRegisterUnload['WebForm'] = function () {
			return message;
		}
		$(window).bind('beforeunload', function() {
			return message;
		});
	}

	// Seitenwechsel erlauben (nachdem Änderungen gespeichert)
	function pageAllowUnload()
	{
		jsLog('pageAllowUnload');
		delete pageRegisterUnload['WebForm'];
		$(window).unbind('beforeunload');
	}
	
	// MultiForm, Wechseln des Objektes, http://stackoverflow.com/questions/6361465/how-to-check-if-click-event-is-already-bound-jquery
	// AKTUELL NICHT VERWENDET
	function pageCanUnload()
	{
		if (!$(window).data('events')) return 1;
		if (!$(window).data('events')['beforeunload']) return 1;
		if (confirm('Änderungen wurden nicht gespeichert.')) return 1;
		return 0;
	}

	// Return: Popup schließen+Script oder zurück zum Referer, müsste auch für WebForm funktionieren
	function pageReturn(referer) 
	{
		pageAllowUnload();
		if (!referer) referer = pageReferer;
		if (!referer) return;
		
		if (referer.substring(0,2) == 'jq') 	eval("close();opener.jqReload('"+referer+"');");
		else if (referer.indexOf('(') != -1) 	eval("close();opener." + referer);
		else if (referer.indexOf(';') != -1) 	eval(referer);						//SP-Designer, Form nicht schließen (OnSuccess)
		else pageLoad(referer);
	}
	
	/**
	 * Cancel: Popup schließen oder zurück zum Referer, müsste auch für WebForm/Infoseite funktionieren
	 * @param referer
	 */
	function pageCancel(referer) 
	{
		pageAllowUnload();
		if (!referer) referer = pageReferer;
		if (!referer) return;

		if (referer.substring(0,2) == 'jq') 	close();
		else if (referer.indexOf('(') != -1) 	close();
		else if (referer.indexOf(';') != -1) 	close();
		else pageLoad(referer);
	}
	
	//SysCell Operation-Fehler 
	//TODO einzelner Zugriff
	//TODO id durch Template vergeben
	function pageSetMessages(info,error,id)
	{
		var html = "";
		if (error) html += "<p class='Wichtig'>"+error+"<p>";
		if (info) html += "<p class='Hinweis'>"+info+"<p>";
		if (!id) id = "comp_topmessages";
		$('#'+id).empty();
		$('#'+id).append(html);
		if (!$('#'+id).length) alert(result);//wird wieder eliminiert, alle Layouts sollen comp_topmessages bereitstellen
	}
	
	/*
	 * document.click wird meist verwendet zum Ausblenden des Popups bei Popup-Controls, wenn außerhalb geklickt wird
	 * sollen sich automatisch bereinigen, dadurch ist sichergestellt, dass benötigte nicht entfernt werden (HDS_MobileButtons nach pageLoad ...)
	 * autocomplete umstellen
	 * im MCE gibt es auch noch ein Problem, Popups werden manchmal nicht gelöscht (document.click bleibt aus) 
	 */
	// 18.1 document.click verteilen, bereinigen
	function pageDocClick(e)
	{
		var l = 0;
		var d = 0;
		for (var i in pageRegisterDocClick) 
		{
			//jsLog('index:'+ i + ' tagName:'+ e.target.tagName ,'pageRegisterDocClick');
			if (typeof pageRegisterDocClick[i] == "function")	pageRegisterDocClick[i](e);
			
			if (!document.getElementById(i) || typeof pageRegisterDocClick[i] != "function") 
			{
				//jsLog('pageRegisterDocClick '+ i +' invalid','pageDocClick'); // Bereinigung Feld ???
				delete pageRegisterDocClick[i];
				d++;
			}
			l++;
		}
		//jsLog('pageRegisterDocClick length:'+ l + ' delete:' + d ,'pageDocClick');		
	}
	$(document).bind('click', pageDocClick);
	
	//reload verhindern?
	//pageBlockUnload();
	//window.onbeforeunload = function () {return 'dsadsad';}
