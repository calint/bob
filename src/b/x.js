debug_set=true;
debug_js=true;
debug_verbose=false;


ui={}
ui.is_dbg=true;
ui.axconwait=false;
$=function(eid){return document.getElementById(eid);}
$d=function(v){console.log(v);}
$s=function(eid,txt){
	var e=$(eid);
	if(debug_set)$d(eid+'{'+txt+'}');
	if(!e){$d(eid+' notfound');return;}
	if(e.nodeName=="INPUT"||e.nodeName=="TEXTAREA"||e.nodeName=="OUTPUT"){
		e.value=txt;
		$b(e);
	}else{
		e.innerHTML=txt;
		if(e.contentEditable=="true")
			$b(e);
	}
}
$o=function(eid,txt){
	if(debug_set)$d(eid+'={'+txt+'}');
	var e=$(eid);if(!e)return;e.outerHTML=txt;
}
$p=function(eid,txt){
	var e=$(eid);
	if(e.nodeName=="INPUT"||e.nodeName=="TEXTAREA"||e.nodeName=="OUTPUT"){
		e.value+=txt;
		$b(e);
	}else{
		e.innerHTML+=txt;
		if(e.contentEditable=="true")
			$b(e);
	}
}
//$l=function(){if(ui.keys)document.onkeyup=ui.onkey;}
$a=function(eid,a,v){$(eid).setAttribute(a,v);}
$r=function(ev,ths,axpb){if(!ev)ev=window.event;$b(ths);if(ev.keyCode!=13)return true;$x(axpb);return false;}
$f=function(eid){var e=$(eid);if(!e)return;if(e.focus)e.focus();/*if(e.select)e.select();*/}
$t=function(s){document.title=s;}
ui.alert=function(msg){alert(msg);}
ui._clnfldvl=function(s){return s.replace(/\r\n/g,'\n').replace(/\r/g,'\n');}
ui._hashKey=function(event){
	var kc=(event.shiftKey?'s':'')+(event.ctrlKey?'c':'')+(event.altKey?'a':'')+(event.metaKey?'m':'')+String.fromCharCode(event.keyCode);
	$d(kc);
	return kc;
}
ui.keys=[];
ui.onkey=function(ev){
	if(!ev)ev=window.event;
	var cmd=ui.keys[ui._hashKey(ev)];
	if(cmd)eval(cmd);
}
ui.fmtsize=function(num){
	return num.toString().replace(/\B(?=(\d{3})+\b)/g,",");
}
ui.fmt_data_per_second=function(nbytes,ms){
	var b_per_s=Math.floor(nbytes*1024/ms);
	if(b_per_s<1024){
		return b_per_s+' B/s';
	}
	b_per_s>>=10;
	if(b_per_s<1024){
		return b_per_s+' KB/s';
	}
	b_per_s>>=10;
	if(b_per_s<1024){
		return b_per_s+' MB/s';
	}
	b_per_s>>=10;
	if(b_per_s<1024){
		return b_per_s+' GB/s';
	}
	b_per_s>>=10;
	if(b_per_s<1024){
		return b_per_s+' TB/s';
	}
}
ui._onreadystatechange=function(){
//	$d(" * stage "+this.readyState);
	var elsts=$('-ajaxsts');
	if(elsts){{var e=elsts;
		if(e._oldbg!=null){
			e.style.background=e._oldbg;
			delete e._oldbg;
		}
	}}
	switch(this.readyState){
	case 1:// Open
		if(this._hasopened)break;this._hasopened=true;//? firefox quirkfix1
		if(debug_verbose)$d(new Date().getTime()-this._t0+" * sending");
		$s('-ajaxsts','sending '+this._pd.length+' text');
		this.setRequestHeader('Content-Type','text/plain; charset=utf-8');
		$d(this._pd);
		ui.req._jscodeoffset=0;
		this.send(this._pd);
		break;
	case 2:// Sent
		var dt=new Date().getTime()-this._t0;
//		$d(dt+" * sending done");
		$s('-ajaxsts','sent '+this._pd.length+' in '+dt+' ms');
		break;
	case 3:// Receiving
//		$d(new Date().getTime()-this._t0+" * reply code "+this.status);
		var s=this.responseText.charAt(this.responseText.length-1);
		var ms=new Date().getTime()-this._t0;
		$s('-ajaxsts','received '+ui.fmtsize(this.responseText.length)+' text '+ui.fmt_data_per_second(this.responseText.length,ms));
//		console.log('receiving '+this.responseText.length+' text');
		if(s!='\n'){
//			$d(new Date().getTime()-this._t0+" * not eol "+(this.responseText.length-this._jscodeoffset));
			break;
		}
		var jscode=this.responseText.substring(this._jscodeoffset);
		if(debug_js)$d(new Date().getTime()-this._t0+" * run "+jscode.length+" bytes");
		if(debug_js)$d(jscode);
		this._jscodeoffset+=jscode.length;
		eval(jscode);
		break;
	case 4:// Loaded
		this._hasopened=null;//? firefox quirkfix1
		this._pd=null;
		ui._pbls=[];

		var jscode=this.responseText.substring(this._jscodeoffset);
		if(jscode.length>0){
			if(debug_js)$d(new Date().getTime()-this._t0+" * run "+jscode.length+" bytes");
			if(debug_js)$d(jscode);
			this._jscodeoffset+=jscode.length;
			eval(jscode);
		}
		this._dt=new Date().getTime()-this._t0;//? var _dt
		$s('-ajaxsts',this._dt+' ms '+ui.fmtsize(this.responseText.length)+' chars '+ui.fmt_data_per_second(this.responseText.length,this._dt));
		$d("~~~~~~~ ~~~~~~~ ~~~~~~~ ~~~~~~~ ")
//		$d("done in "+this._dt+" ms");
		break;		
	}
}
ui._pbls=[];
debug_qpb=true;
ui.qpb=function(e){
	if(debug_qpb)$d('qpb '+e.id);
	if(ui.qpbhas(e.id))return;
	ui._pbls[e.id]=e.id;
}
$b=ui.qpb;
ui.qpbhas=function(id){return id in ui._pbls;}
ui._axc=1;
$x=function(pb){
	ui._axc++;
	$d("\n\nrequest #"+ui._axc);
	var post='$='+pb+'\r';
	for(var id in ui._pbls){
		//$d('field '+id);
		var e=$(id)
		post+=e.id+'=';			
		if(e.value!==undefined)
			post+=ui._clnfldvl(e.value);
		else{
			post+=ui._clnfldvl(e.innerHTML);
		}
		post+='\r';
	}
	$d("~~~~~~~ ~~~~~~~ ~~~~~~~ ~~~~~~~ ")
	if(!ui.req){
		ui.req=new XMLHttpRequest();
		ui.req.onreadystatechange=ui._onreadystatechange;
		ui.req.onerror=function(){
			var e=$('-ajaxsts');
			if(!e)return;
			e._oldbg=e.style.background;
			e.style.background='#f00';
			$s('-ajaxsts','connection to server lost. try reload or wait and re-try.');
		}
		$s('-ajaxsts'," * new connection");
	}else{
		$s('-ajaxsts'," * reusing connection");
		var count=0;
		while(ui.req.readyState==1||ui.req.readyState==2||ui.req.readyState==3){
			if(ui.axconwait){
				$d("  * busy, waiting");
				alert("connection busy. waiting.");
				count++;
				if(count>3)
					throw "waiting cancelled";
			}else{
				$d("   * busy, cancelling");
				ui.req.abort();
				ui.req._hasopened=null;//? firefox quirkfix1
			}
		}	
	}
	ui.req._t0=new Date().getTime();
	ui.req._pd=post;
	ui.req.open('post',location.href,true);
}