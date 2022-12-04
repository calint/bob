debug_set=true;
debug_js=true;
debug_verbose=false;


ui={}
ui.is_dbg=true;
ui.axconwait=false;
$=function(eid){return document.getElementById(eid);}
$d=function(v){console.log(v);}
$s=function(eid,txt){
	const e=$(eid);
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
	const e=$(eid);if(!e)return;e.outerHTML=txt;
}
$p=function(eid,txt){
	const e=$(eid);
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
$f=function(eid){const e=$(eid);if(!e)return;if(e.focus)e.focus();/*if(e.select)e.select();*/}
$t=function(s){document.title=s;}
ui.alert=function(msg){alert(msg);}
ui._clnfldvl=function(s){return s.replace(/\r\n/g,'\n').replace(/\r/g,'\n');}
ui._hashKey=function(event){
	const kc=(event.shiftKey?'s':'')+(event.ctrlKey?'c':'')+(event.altKey?'a':'')+(event.metaKey?'m':'')+String.fromCharCode(event.keyCode);
	$d(kc);
	return kc;
}
ui.keys=[];
ui.onkey=function(ev){
	if(!ev)ev=window.event;
	const cmd=ui.keys[ui._hashKey(ev)];
	if(cmd)eval(cmd);
}
ui.fmtsize=function(num){
	return num.toString().replace(/\B(?=(\d{3})+\b)/g,",");
}
ui.fmt_data_per_second=function(nbytes,ms){
	let b_per_s=Math.floor(nbytes*1024/ms);
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
	$d("\nmessage #"+ui._axc);
	$d("~~~~~~~ ~~~~~~~ ~~~~~~~ ~~~~~~~ ")
	let post='$='+pb+'\r';
	for(var id in ui._pbls){
		//$d('field '+id);
		const e=$(id)
		post+=e.id+'=';			
		if(e.value!==undefined)
			post+=ui._clnfldvl(e.value);
		else{
			post+=ui._clnfldvl(e.innerHTML);
		}
		post+='\r';
	}
	$d(post.replace("\r","\n"));
	$d("~~~~~~~ ~~~~~~~ ~~~~~~~ ~~~~~~~ ")
	ws.send(post);
}