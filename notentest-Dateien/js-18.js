/*------------------------------------------------------------------------------------------
2010-2018 Falk Neuner
/*------------------------------------------------------------------------------------------
Usage: JS-Code für synamischen Laden (JQ)
Model: JS-lib
/*------------------------------------------------------------------------------------------
 * 18.1 jsReplaceAll
 * 18.2 jsEnableCookies, jsStartsWith, jsHasClass
------------------------------------------------------------------------------------------*/
if (jsMobileMenus == undefined)
{
	// Mobile Popups registrieren, z.B. siehe HDS_MobileButtons
	var jsMobileMenus = new Array();
	var jsPageCookies = new Array();
	var jsEnableLog = null;
	//var jsEnableCookies = false;
}
var jsEnableCookies = undefined;

/**
 * Cookie setzen
 * @param name
 * @param value Leerstring löscht Cookie
 * @param expires Tage
 * @param path, wenn nicht angegeben, dann wird aktueller ordner verwendet
 * @param domain
 * @param secure
 */
function jsSetCookie(name,value,expires,path,domain,secure) { 
	
	// 18.2
	if (typeof jsEnableCookies != 'undefined' && !jsEnableCookies) return;
	
	//path = document.location.pathname;
	if (value == 'undefined') value = '';
	if (value == 'null') value = '';
	if (value == '') expires = -1; // löscht Cookie	
	jsLog(name+'='+value+' path:'+path,'jsSetCookie');

	expires = expires * 60*60*24*1000;
	var today = new Date();
	var expires_date = new Date( today.getTime() + (expires) );
    var cookieString = name + "=" +escape(value) + 
       ( (expires) ? ";expires=" + expires_date.toGMTString() : "") + 
       ( (path) ? ";path=" + path : "") + 
       ( (domain) ? ";domain=" + domain : "") + 
       ( (secure) ? ";secure" : ""); 
    document.cookie = cookieString; 
    
    // 17.1 nach AJAX-Seitenwechsel ist document.cookie nicht aktuell
    if (document.location.pathname == path) jsPageCookies[name] = value;
}

/*
 * jsGetCookie geht bei lokalem Cookie nach pageLoad nicht, da document.cookie der alte bleibt
 * Cookies werden meistens serverseitig gelesen, 
 * wird aber nur an wenigen Punkten cleintseitig gelesen (jsGetCookie), meistens unkritisch
 * AC-Suche
 * SP-Designer
 * HisSelectors
 * */
/**
 * Cookie lesen
 * @param name
 * @param def
 * @returns
 */
function jsGetCookie(name,def) {
    // 17.1 AJAX-Seitenwechsel
	if (jsPageCookies[name]) return jsPageCookies[name];
	
	var start = document.cookie.indexOf(name+"="); 
	var len = start+name.length+1; 
	if ((!start) && (name != document.cookie.substring(0,name.length))) return def; 
	if (start == -1) return def; 
	var end = document.cookie.indexOf(";",len); 
	if (end == -1) end = document.cookie.length; 
	var value = unescape(document.cookie.substring(len,end)); 
	if (value == 'undefined') value = undefined;
	if (value == 'null') value = undefined;
	return value;
} 

function jsConfirmGoto(info,href) 
{
	if (!confirm(info)) return;
	window.location.href = href;
}
/* 16.4
function jsEncLoc(base64) 
{
	window.location.href = atob(base64);
}
*/

/*------------------------------------------------------------------------------------------
console.log in IE muss unter bestimmten Bedingungen abgeschalten werden
------------------------------------------------------------------------------------------*/
function jsLog(info,context) 
{
	//if (!jsEnableLog) return;
	if (navigator.userAgent.indexOf("MSIE") > -1) return;
	
	if (context) console.log(context+' '+info);
	else console.log(info);
}

/*------------------------------------------------------------------------------------------
Token hinzufügen/entfernen
TODO prüfen warum führendes Komma erforderlich und eliminieren !!!
------------------------------------------------------------------------------------------*/
/**
 * Token hinzufügen
 * @param list
 * @param token
 * @param max
 * @returns {String}
 */
function jsAddToken(list,token,max)
{
	if (list == undefined) list = ',';
	if (list == ',') list = ',';
	if (list[0] != ',') list = ',' + list;
	list = list.replace(',' + token + ',',',') + token + ',';
	
	var c = 0;
	for (var i = list.length -1; i > 0; i--)
	{
		if (list[i] == ',') 
		{
			if (c == max) return list.substring(i);
			c++;
		}
	}
	return list;
}

/**
 * Token aus Liste löschen
 * @param list
 * @param token
 * @returns {String}
 */
function jsDelToken(list,token)
{
	if (list == undefined) list = ',';
	if (list == ',') list = ',';
	if (list[0] != ',') list = ',' + list;
	list = list.replace(',' + token + ',',',');
	if (list == ',') list = '';
	return list;
}

/**
 * Anzahl Token ermitteln
 * @param list
 * @returns {Number}
 */
function jsCountToken(list)
{
	var count = 0;
	for (var i = 0; i < list.length; i++)
	{
		if (list[i] == ',') count++;
	}
	return count;
}

/**
 * Vorhandensein Token prüfen
 * @param list
 * @param token
 * @returns {Boolean}
 */
function jsHasToken(list,token)
{
	return list.indexOf(',' + token + ',') != -1;
}

/**
 * CSS-Klasse hinzufügen, ähnlich jqAddClass()
 * @param node Element
 * @param token CSS-Klasse
 */
function jsAddClass(node,token)
{
	var list = node.className;
	list = ' ' + list + ' ';
	list = list.replace(' ' + token + ' ',' ') + token + ' ';
	node.className = list.trim();
}

/**
 * 18.2 CSS-Klasse prüfen
 * @param node
 * @param name
 * @returns {Boolean}
 */
function jsHasClass(node,name)
{
	var list = node.className;
	list = ' ' + list + ' ';
	return list.indexOf(' ' + name + ' ') != -1;
}

/**
 * CSS-Klasse entfernen, ähnlich jqRemoveClass()
 * @param node Element
 * @param token CSS-Klasse
 */
function jsDelClass(node,token)
{
	var list = node.className;
	list = ' ' + list + ' ';
	list = list.replace(' ' + token + ' ',' ');
	node.className = list.trim();
}

/**
 * 18.1 alle Vorkommen ersetzen
 * @param text
 * @param remove
 * @param insert
 * @returns
 */
function jsReplaceAll(text,remove,insert)
{
	if (!text) return text;
	while (text.indexOf(remove) > -1) text = text.replace(remove,insert);
	return text;
}

/**
 * 18.2 JS startsWith fehlt in Safari 5 (Windows), vor IE12
 * @param text
 * @param start
 * @returns Boolean
 */
function jsStartsWith(text,start)
{
	return text.substring(0,start.length) == start;
}

/**
 * verhindert Ausführung unterlagerter onlick 
 */
function jsStopPropagation()
{
	e = window.event;
	if (e)
	{
		if (e.stopPropagation) e.stopPropagation();//IE9 & Other Browsers
		else e.cancelBubble = true;//IE8 and Lower
	}
}

/**
 * UTF8 erzeugen
 * bei clientseitigem Setzen eines Cookies mit Umlauten verwenden
 * http://phpjs.org/functions/utf8_encode/
 * @param argString Text
 */
function jsUtf8Encode (argString) 
{
	if (argString === null || typeof argString === "undefined") 
	{
		return "";
	}

	var string = (argString + ''); // .replace(/\r\n/g, "\n").replace(/\r/g, "\n");
	var utftext = '', start, end, stringl = 0;
	start = end = 0;
	stringl = string.length;
	for (var n = 0; n < stringl; n++) {
		var c1 = string.charCodeAt(n);
		var enc = null;

		if (c1 < 128) {
			end++;
		} else if (c1 > 127 && c1 < 2048) {
			enc = String.fromCharCode((c1 >> 6) | 192, (c1 & 63) | 128);
		} else {
			enc = String.fromCharCode((c1 >> 12) | 224, ((c1 >> 6) & 63) | 128, (c1 & 63) | 128);
		}
		if (enc !== null) {
			if (end > start) {
				utftext += string.slice(start, end);
			}
			utftext += enc;
			start = end = n + 1;
		}
	}
	if (end > start) {
		utftext += string.slice(start, stringl);
	}
	return utftext;
}