/*------------------------------------------------------------------------------------------
Copyright 2003-2017 Falk Neuner, nsoft.de
Ausführung, Bearbeitung, Unterlizensierung nur mit ausdrücklicher Erlaubnis des Urhebers
/*------------------------------------------------------------------------------------------
Usage: Popup-Fenster
Model: JS-lib
/*------------------------------------------------------------------------------------------
 * 17.3 Cookie komprimiert
------------------------------------------------------------------------------------------*/
	/**
	 * Cookie wird für (opener)Dokument angelegt
	 * @param width
	 * @param heigth
	 * @param left
	 * @param top
	 * @param name
	 * @returns
	 */
	function popupResizeCallback(width,heigth,left,top,name)
	{
		jsSetCookie(name+'PopupPos',width+'/'+heigth+'/'+left+'/'+top,10);
	}
	
	// Sichern Fensterposition bei Schließen 16.2
	window.onunload = function(event) 
	{
		if (opener != undefined && opener.popupResizeCallback != undefined && window.name != undefined)
		{
			//opener.alert('inner:'+window.innerWidth+' outer:'+window.outerWidth);
			
			// Chrome: Screen pixel mit Rand 
			//opener.popupResizeCallback(window.outerWidth,window.outerHeight,window.screenX,window.screenY,window.name);
			
			// CSS Pixel
			opener.popupResizeCallback(window.innerWidth,window.innerHeight,window.screenX,window.screenY,window.name);
		}
	}

/*--------------------------------------------------------
ab MSIE 8/10/11: opener wird bei erneutem open() nicht überschrieben (Sicherheits-Feature), anstelle dessen auf closed gesetzt
damit wird opener ungültig (APL-Liste > Inspektor > APL-Liste).
Weiterhin kann man im IE über den open()-return nicht auf statische Methoden zurückgreifen.

1. Lösung: Inspektor muss sich registrieren, damit vor open() ein close() aufgerufen werden kann.
2. Lösung: wenn Inspektor ohne Postbacks auskommt, können eventuell nach open() per Funktion Varianblen übermittelt werden, bleiben erhalten
3. Inpektor fragt beim Root-Fenster die APL-Liste an
--------------------------------------------------------*/
	var popupWindows = new Array();
	function getRoot()
	{
		if (window.popupRoot != undefined) return window.popupRoot;
		return window;
	}
	function setWindow(_name,_window)
	{
		popupWindows[_name] = _window;
	}
	function getWindow(_name)
	{
		return popupWindows[_name];
	}
	function popupHasOpener()
	{
		return opener != undefined && !opener.closed;
	}
	function touch(_window)
	{
		window.popupRoot = _window;
	}
	function hello()
	{
		alert('hello');
	}	
	
	// MSIE: Registrierung aller Popups beim Root-Fenster
	if (navigator.userAgent.indexOf("MSIE") != -1)
	{
		if (popupHasOpener() && window.popupRoot == undefined) 
		{
			window.popupRoot = opener.getRoot();
			window.popupRoot.setWindow(name,window);
		}
	}

//TODO Cookies zusammenführen
/*--------------------------------------------------------
popupOpen darf kein Rückgabewert haben, irritiert SeaMonkey
in Chrome werden mehrer Monitore nicht unterstützt, ermittelte linke Position ist Monitor-übergreifend (0-4000)
popup kann aber nich auf anderm Monitor geöffnet werden
http://stackoverflow.com/questions/6754260/popup-open-position-in-chrome
--------------------------------------------------------*/
	function popupOpen(url,name,_width)
	{
		if (_width == undefined) _width = 710;
		if (!_width) _width = 710;
		var WHLT = 		jsGetCookie(name+'PopupPos',_width+'/900/10/10').split('/');
		var width = 	WHLT[0];
		var height = 	WHLT[1];
		var left = 		WHLT[2];
		var top = 		WHLT[3];
		jsLog(name+' '+WHLT+' '+url,'popupOpen');
		
		// im MSIE close() auf gleichnamiges Fenster ausführen (siehe oben) 
		// damit wird Fenster neu erstellt und opener neu gesetzt wird (APL/Baumann)
		// 17.1 closed prüfen (sonst Fehler bei wenn APL-Objektfendter zuvor geschlossen wurde)
		if (navigator.userAgent.indexOf("MSIE") != -1) if (window.popupRoot != undefined) 
		{		
			var popup = window.popupRoot.getWindow(name);
			if (popup && !popup.closed && !popup.popupHasOpener()) popup.close();
		}
		
		/*
		 * Chrome outerWidth screen pixel, innerWidth CSS pixel, 100,110,125,150, open verlangt screen pixel
		 * Zoom feststellen, open muss in screenpixel angegeben werden.
		 * die Umrechnung funktioniert, nach mehrmaligenm Öffnen/Schließen wird des Fenster immer etwas höhher (1 px)
		 */
		//alert(navigator.userAgent+' '+window.devicePixelRatio);
		//alert(window.outerWidth+' '+window.innerWidth);
		
		// Erkennung Browerzoom bei Chrome
		var ratio1 = window.outerWidth / window.innerWidth;
		
		//im MCE funktioniert oben nicht (verm. wegen Frames) 1920/14## ergibt 1.25
		if (name == 'link_' || (window.parent && window.parent != window)) 
		{
			ratio1 = 1; 
			//jsLog(window.outerWidth +'/'+ window.innerWidth +' ratio2 '+ratio2,'popupOpen');
			//jsLog(window.parent.window.outerWidth +'/'+ window.parent.window.innerWidth +' ratio2 '+ratio2,'popupOpen');
			//jsLog(window.parent.window.parent.window.outerWidth +'/'+ window.parent.window.parent.window.innerWidth +' ratio2 '+ratio2,'popupOpen');
			//jsLog(window.parent.parent.parent.outerWidth +'/'+ window.parent.parent.parent.innerWidth +' ratio2 '+ratio2,'popupOpen');
		}
		
		var ratio2 = 1;
		if (ratio1 > 1.4) ratio2 = 1.5;
		else if (ratio1 > 1.2) ratio2 = 1.25;
		else if (ratio1 > 1.05) ratio2 = 1.1;
		
		// Edge window.outerWidth window.innerWidth; scheine gleich zu sein (18) und CSS-Pixel, open verlangt screen pixel
		if (navigator.userAgent.indexOf("Edge/") != -1) ratio2 = window.devicePixelRatio;
				
		// bei Zoom 150% wurde h immer 3 px größer Chrome
		
		width = width * ratio2;
		height = height * ratio2;
		width = width - (width%10);
		height = height - (height%10);
		
		var f1 = open(url,name,'width='+width+',height='+height+',top='+top+',left='+left+',scrollbars=yes,resizable=yes');
		//alert(width);
		if (!f1)
		{
			alert('Popup konnte nicht geöffnet werden. Möglicherweise wurde das Popup durch Ihren Browser blockiert. Um die Funktion zu nutzen, müssen Sie Popups für diese Website zulassen.');
			//return 0;
		}
		f1.focus();
		
		// Variablen einsetzen
		// funktioniert vermutlich, allerdings geht popupRoot durch Submit im APL-Inspektor verloren
		// wenn APL-Inspektor ohne Submit auskommt, sollte das Einsetzen funktionieren
		//if (ff != undefined) ff.touch(window.popupRoot);
	}
