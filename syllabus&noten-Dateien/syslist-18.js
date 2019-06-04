/*------------------------------------------------------------------------------------------
Copyright 2003-2014 Falk Neuner, nsoft.de
Ausführung, Bearbeitung, Unterlizensierung nur mit ausdrücklicher Erlaubnis des Urhebers
/*------------------------------------------------------------------------------------------
Usage: JS-Code für SysList
Model: JS-lib
/*------------------------------------------------------------------------------------------
 * 16.4 OPs auf arge
 * 17.3 komprimierter Cookie
------------------------------------------------------------------------------------------*/
		 
		/* 
		 * syslist.js 
		 * Objektdaten als nicht objektorientierte Lösung
		 * TODO Umbau auf Klassen syslistClass, siehe pageEventClass
		 */
		var SysList_columns = new Array();	// Spaltenliste
		var SysList_load = new Array();		// Lademodus Spaltensichtbarkeit
		var SysList_suff = new Array();
		var SysList_size = new Array();
		var SysList_cb_form = new Array();	// Wegfall geplant
		var SysList_cb_field = new Array();	// Wegfall geplant
		var SysList_callback = new Array();
		var SysList_sortFct = new Array();	// Extension-eigene Sortierfunktion (MultiObjectEdit)

		/**
		 * Control-Status: Wert setzen
		 * @param canvas
		 * @param key
		 * @param val
		 */
		function SysList_SetValue(canvas,key,val)
		{
			var _canvas = canvas.replace('_table',''); //EmmiFPList
			jqCanvasValues[_canvas][key] = val;
			jqRequestValues[_canvas] = JSON.stringify(jqCanvasValues[_canvas]);
			
			// 17.3 Dirty-Erkennung geänderte Mehrfachauswahl im Reitermodus (APL-Reitermodell)
			//if (key == 'list')
			//{
			//	jqContextCallback(key,val);
			//}
		}
		
		/**
		 * // Control-Status: Wert lesen
		 * @param canvas
		 * @param key
		 * @returns
		 */
		function SysList_GetValue(canvas,key)
		{
			var _canvas = canvas.replace('_table',''); //EmmiFPList
			return jqCanvasValues[_canvas][key];
		}

		/**
		 * komprimierten Cookie erzeugen (Zustandsmanagement CMS:7479)
		 * @param canvas
		 * @param key
		 * @param val
		 */
		function SysList_SetCookie(canvas,key,val)
		{
			// 17.3 
			jqCanvasCookie[canvas][key.replace('page_','')] = val;
			var syslist = JSON.stringify(jqCanvasCookie[canvas]);
			jqSetCookie(canvas,'syslist',syslist);
			
			// alten Cookie löschen
			//jqSetCookie(canvas,key,val);
			jqSetCookie(canvas,key,'');
		}
		
// TODO call sollte nicht im Status bleiben
		/**
		 * Paging 
		 * @param canvas
		 * @param argc
		 * @param block
		 * @param size
		 * @param count
		 */
		function SysList_Paging(canvas,argc,block,size,count)
		{
			var _canvas = canvas.replace('_table','');
			if (block != undefined)
			{
				SysList_SetCookie(_canvas,'page_block',block);
				SysList_SetCookie(_canvas,'page_size',size);

				SysList_SetValue(_canvas,'block',block);
				SysList_SetValue(_canvas,'size',size);
				SysList_SetValue(_canvas,'call','page');
			}
			
			// Reload
			jqReloadVars(canvas,argc);
			
			// clientseitige Status aktualisieren im scroll-Modus
			if (count)
			{
				pages = Math.ceil(count/size);
				if (block > 0)
					if (block < (pages-1)*size) c = 'mid';
					else c = 'last';
				else 
					if (pages>1) c = 'first';
					else c = 'no';
				if (document.getElementById(_canvas+'/SysPaging0')) document.getElementById(_canvas+'/SysPaging0').className = 'SysPaging ' + c;
				if (document.getElementById(_canvas+'/SysPaging1')) document.getElementById(_canvas+'/SysPaging1').className = 'SysPaging ' + c;
			
				if (count-block < size) last = count;
				else last = block + size;
				first = block + 1;
				if (document.getElementById(_canvas+'/PagingInfo0')) document.getElementById(_canvas+'/PagingInfo0').innerHTML = first + '-' + last;
				if (document.getElementById(_canvas+'/PagingInfo1')) document.getElementById(_canvas+'/PagingInfo1').innerHTML = first + '-' + last;
				if (document.getElementById(_canvas+'/PagingInfo')) document.getElementById(_canvas+'/PagingInfo').innerHTML = first + '-' + last;
			}
		}

		/**
		 * Sortierung ändern
		 * @param canvas
		 * @param argc
		 * @param sort
		 */
		function SysList_Sorting(canvas,argc,sort) 
		{
			if (SysList_sortFct[canvas])
			{
				SysList_sortFct[canvas](canvas,argc,sort);
				return;
			}
			
			var _canvas = canvas.replace('_table','');
			if (sort != undefined)
			{
				SysList_SetCookie(_canvas,'page_sort',sort);//TODO wird seitenweise gesetzt
				SysList_SetValue(_canvas,'sort',sort);
				SysList_SetValue(_canvas,'call','sort');
			}
			jqReloadVars(canvas,argc);
		} 

		/**
		 * Anforderung ORL-Report
		 * @param canvas
		 * @param sysUploadId
		 * @param frame
		 */
		function SysList_download(canvas,sysUploadId,frame)
		{
			var vars = new Array();
			var argc = jqCanvasArgument[canvas]; // wegen CanvasTicket
			vars['canvas'] = canvas;
			vars['argc'] = argc;
			vars['SysUploadId'] = sysUploadId;
			jqRedirectVars(jqCanvasArgument[canvas],vars,frame);
		}
		
		function SysList_SaveListState(canvas,list)
		{
			var state = {};
			state['list'] = list;
			state['callbackField'] = SysList_GetValue(canvas,'callbackField');//SysList_cb_field[canvas];
			state['callback'] = SysList_GetValue(canvas,'callback');//SysList_callback[canvas];//'opener.MultiSelectControl_set';
			jsLog(JSON.stringify(state),'SysList_SaveListState');
			jsSetCookie('nsoft_state',JSON.stringify(state),60*60*24*30*1000,jqGetPath());
		}

		/**
		 * Mehrfachauswahl ändern
		 * @param canvas
		 * @param id
		 * @param checked
		 */
		function SysList_checked(canvas,id,checked)
		{
			//tr-Selektion verhindern
			jsStopPropagation();
			
			canvas = canvas.replace('_table','');
			SysList_SetValue(canvas,'call','check');
			var ids = SysList_GetValue(canvas,'list');
			list = ',' + SysList_GetValue(canvas,'list');
			list = list.replace('undefined','');
			list = list.replace(','+ id +',',',').substring(1);
			if (checked) 
			{
				list = list + id + ',';
				SysList_SetValue(canvas,'list',list);
				//jqReloadVars(canvas + SysList_suff[canvas],jqCanvasArgument[canvas]);// check muss im äußeren div ausgeführt werden, wegen 
				jqReloadVars(canvas,jqCanvasArgument[canvas]);
			}
			else
			{
				SysList_SetValue(canvas,'list',list);
				//jqReloadVars(canvas + SysList_suff[canvas],jqCanvasArgument[canvas]);// check muss im äußeren div ausgeführt werden
				jqReloadVars(canvas,jqCanvasArgument[canvas]);
			}
			
	        //State speichern EXPERIMENTELL
			SysList_SaveListState(canvas,list);
			
			// aktuelle Auswahl HisAlumniGrid
			$('#cbinfo').empty();
			$('#cbinfo').append(list.split(',').length-1);
		}		
		
		/**
		 * alle angezeigten zu Mehrfachselektion hinzufügen (nicht angezeigte werden entfernt)
		 * @param canvas
		 */
		function SysList_checkAll(canvas)
		{
			canvas = canvas.replace('_table','');
			var inputs = document.getElementById(canvas + SysList_suff[canvas]).getElementsByTagName("input"); // nur im inneren div suchen
			var list = '';
			for (var i = 0; i < inputs.length; i++) 
			{
				if (inputs[i].type == "checkbox") 
				{
					list = list + inputs[i].value + ',';
				}
			}
			SysList_SetValue(canvas,'call','check');
			SysList_SetValue(canvas,'list',list);
			SysList_SaveListState(canvas,list);
			//jqReloadVars(canvas + SysList_suff[canvas],jqCanvasArgument[canvas]);// check muss im äußeren div ausgeführt werden
			jqReloadVars(canvas,jqCanvasArgument[canvas]);
		}
		
		/**
		 * alle aus Mehrfachselektion entfernen
		 * @param canvas
		 */
		function SysList_uncheckAll(canvas)
		{
			canvas = canvas.replace('_table','');
			SysList_SetValue(canvas,'call','check');
			SysList_SetValue(canvas,'list','');
			SysList_SaveListState(canvas,'');
			//jqReloadVars(canvas + SysList_suff[canvas],jqCanvasArgument[canvas]);// check muss im äußeren div ausgeführt werden
			jqReloadVars(canvas,jqCanvasArgument[canvas]);
		}

		// Rücklieferung Mehrfachauswahl an Formular submit-Modus, 
		//TODO Verfahren umstellen auf JQ (siehe APL)
		//TODO Apply/Rücklieferung Einfach/Mehrfachauswahl harminsieren auf return=
		function SysList_ApplySelection(canvas,list) 
		{
			if (SysList_callback[canvas]) 
			{
				var noclose = SysList_callback[canvas](SysList_cb_field[canvas],list);
				if (noclose == undefined || noclose == false) close();
			}
			else if (opener.document.forms[SysList_cb_form[canvas]])
			{
				opener.document.forms[SysList_cb_form[canvas]].elements[SysList_cb_field[canvas]].value = list;
				opener.document.forms[SysList_cb_form[canvas]].submit();
				close();
			}
		}
		
		/**
		 * 16.3 Operation auf Mehrfachauswahl ausführen
		 * @param canvas
		 * @param argc
		 * @param operation
		 * @param opname ! oder ? aktivieren Bestätigugnsdialog
		 */
		function SysList_MultiOperation(canvas,argc,operation,opname)
		{
			if (!operation) return;
			var list = SysList_GetValue(canvas,'list');

			// ohne Bestätigugnsdialog
			if (opname.indexOf('!') == -1 && opname.indexOf('?') == -1)
			{
				var arge = new Array();  
				arge['GridOperation'] = operation;
				jqReloadVars(canvas,argc,arge);
				jsSetCookie('nsoft_state','');
				return;
			}
			
			// mit Bestätigugnsdialog
			jqModalMessage('Operation <b>'+opname+'</b> für <b>'+jsCountToken(list)+'</b> Objekte ausführen ?',function() {
				var arge = new Array();  
				arge['GridOperation'] = operation;
				jqReloadVars(canvas,argc,arge);
				jsSetCookie('nsoft_state','');
			});
		}

		/**
		 * Umschaltung Extension 15.4, 
		 * läuft eventuell aus > SearchForm_ReloadPage()
		 * @param canvas
		 * @param modus
		 */
		function SysList_Switch(canvas,modus) 
		{
			modus = modus.replace('.',':').split(':')[0];
		    jqCanvasArgument[canvas] = modus+':'; 			
 		}
		
		/**
		 * Absenden Suchformular, gehört eigentlich zu SearchForm
		 * @param canvas
		 * @param noreset
		 */
		function SysList_Submit(canvas,noreset) 
		{
			SysList_SetValue(canvas,'call','submit');
			if (!noreset)
			{
				SysList_SetValue(canvas,'block','0');
				SysList_SetValue(canvas,'size',SysList_size[canvas]);
				SysList_SetValue(canvas,'sort',null);
			
				SysList_SetCookie(canvas,'page_block','0');
				SysList_SetCookie(canvas,'page_sort','');
				SysList_SetCookie(canvas,'page_size','');
			}
			
			// Submit
			jqSubmit(canvas,jqCanvasArgument[canvas]);

//TODO müsste entkoppelt werden JS-Kontext) 18.4 Such-Popup schließen (Mobile)
			var layoutBody = document.getElementById('layoutBody');
			if (layoutBody && jsHasClass(layoutBody,'MobilePopupOpen')) return;
			if (typeof HDS_MobileButtons_switch === "function") HDS_MobileButtons_switch(0);
			if (typeof CmsMobileButtons_switch === "function") CmsMobileButtons_switch(0);
		}
		
		/**
		 * Rücksetzen Suchformular
		 * @param canvas
		 */
		function SysList_Reset(canvas)
		{
			SysList_SetValue(canvas,'call',null);
			SysList_SetValue(canvas,'block','0');
			SysList_SetValue(canvas,'size',SysList_size[canvas]);
			SysList_SetValue(canvas,'sort',null);

			SysList_SetCookie(canvas,'page_block','0');
			SysList_SetCookie(canvas,'page_sort','');
			SysList_SetCookie(canvas,'page_size','');
			jqResetForm(canvas,jqCanvasArgument[canvas]);
		}

		// identisch jqReload
		function SysList_Reload(canvas)
		{
			jqReloadVars(canvas,jqCanvasArgument[canvas.replace('_table','')]);
		}
		
//TODO MOption_ id müsste mit canvas versehen sein
		/**
		 * Anwendung Spaltenstatus
		 * @param canvas
		 */
		function SysList_Loaded(canvas) 
		{
			var hidden = SysList_GetValue(canvas,'hidden');
			var init_hidden = ',';
			var columns = SysList_columns[canvas];
			jsLog(canvas + ' c:' + columns + ' h:' + hidden,'SysList_Loaded') 
			for (var i in columns)
			{
				var col = columns[i];
				if (!hidden && $('th.'+col).css('display') == 'none')
				{ 
					if (document.getElementById('MOption_'+col)) document.getElementById('MOption_'+col).checked = false;
					init_hidden = init_hidden + col + ',';
				}
				if (SysList_load[canvas] != 'server' && hidden)
				{
					var display = 'table-cell';
					if (jsHasToken(hidden,col)) display = 'none';
					$('#'+canvas+' .AppGrid .'+col).css('display',display);	
				}
			}
			if (!hidden)
			{
				SysList_SetValue(canvas,'hidden',init_hidden);
			}
		}
		
		/**
		 * Rücksetzen Spaltenstatus auf CSS rücksetzen,
		 * 17.2 Medienportal differenzieren mit pagePortal
		 * @param canvas
		 */
		function SysList_ColumnReset(canvas)
		{
			SysList_SetValue(canvas,'call',null);
			SysList_SetValue(canvas,'hidden','');
			SysList_SetCookie(canvas,pagePortal + 'page_hidden','');
			SysList_Reload(canvas);
		}

		/**
		 * Spaltenstatus umschalten, 
		 * 17.2 Medienportal differenzieren mit pagePortal
		 * @param canvas
		 * @param col
		 * @param checked
		 */
		function SysList_Column(canvas,col,checked)
		{
			var hidden = SysList_GetValue(canvas,'hidden');
			if (checked) 
			{
				hidden = jsDelToken(hidden,col); 
				$('#'+canvas+' .AppGrid .'+col).css('display','table-cell');
			}
			else
			{
				hidden = jsAddToken(hidden,col,100);
				$('#'+canvas+' .AppGrid .'+col).css('display','none');
			}
			if (hidden == '') hidden = ',';// , muss stehenbaleiben, sonst ist alles rückgesetzt
			SysList_SetValue(canvas,'hidden',hidden);
			SysList_SetCookie(canvas,pagePortal + 'page_hidden',hidden);
		}
		
		/**
		 * einzelnes Objekt löschen (Delete) mit Bestätigugnsdialog
		 * @param canvas
		 * @param argc
		 * @param ElementId
		 * @param ElementName
		 * @param ElementClass
		 */
		function SysList_DeleteElement(canvas,argc,ElementId,ElementName,ElementClass)
		{
			//tr-Selektion verhindern, onclick=DeleteElement... erforderlich
			jsStopPropagation();

			if (ElementClass == undefined) ElementClass = 'Eintrag';
			jqModalMessage(ElementClass+' <b>'+ElementName+'</b> löschen ?',function() {
				// 16.4 OPs von jqRequestValues auf arge umstellen, damit flüchtig
				var arge = new Array();  
				arge['GridOperation'] = 'Delete';
				arge['GridElementId'] = ElementId;
				jqReloadVars(canvas,argc,arge);
			});
		}
		
		/**
		 * Liste schließen (Mehrfachauswahl, Änderungen werden verworfen)
		 */
		function SysList_close()
		{
			pageAllowUnload();
			close();
		}

	