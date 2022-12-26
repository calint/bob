ui={}
ui.is_dbg=true;
ui.is_dbg_set=true;
ui.is_dbg_pb=true;
$=function(eid){return document.getElementById(eid);}
$d=function(v){console.log(v);}
$s=function(eid,txt){
	// extract inline script
	const tag_bgn='<script>';
	const tag_bgn_len=tag_bgn.length;
	const tag_end='</script>';
	const tag_end_len=tag_end.length;
	const inlines=[];
	let i=0;
	while(true){
		const i_bgn=txt.indexOf(tag_bgn,i);
		if(i_bgn==-1)
			break;
		const i_end=txt.indexOf(tag_end,i_bgn+tag_bgn_len);
		if(i_end==-1){
			alert('Did not find end tag after index '+(i_bgn+tag_bgn_len));
			break;
		}
		const script=txt.substring(i_bgn+tag_bgn_len,i_end);
		inlines.push(script);
		txt=txt.substring(0,i_bgn)+txt.substring(i_end+tag_end_len,txt.length);
		i=i_bgn;
	}
	// set txt stripped of inline script
	const e=$(eid);
	if(ui.is_dbg_set)$d(eid+'{'+txt+'}');
	if(!e){$d(eid+' notfound');return;}
	if(e.nodeName=='INPUT'||e.nodeName=='TEXTAREA'||e.nodeName=='OUTPUT'){
		e.value=txt;
	}else{
		e.innerHTML=txt;
	}

	for(let i=0;i<inlines.length;i++){
		console.log('inline: '+inlines[i]);
		eval(inlines[i]);
	}
}
$sv=function(eid,txt){
	const e=$(eid);
	if(ui.is_dbg_set)$d(eid+'{'+txt+'}');
	if(!e){$d(eid+' notfound');return;}
	if(e.nodeName=='INPUT'||e.nodeName=='TEXTAREA'||e.nodeName=='OUTPUT'){
		e.value=txt;
		$b(e);
	}else{
		e.innerHTML=txt;
		if(e.contentEditable=='true')
			$b(e);
	}
}
$o=function(eid,txt){
	if(ui.is_dbg_set)$d(eid+'={'+txt+'}');
	const e=$(eid);if(!e)return;e.outerHTML=txt;
}
$p=function(eid,txt){
	const e=$(eid);
	if(e.nodeName=='INPUT'||e.nodeName=='TEXTAREA'||e.nodeName=='OUTPUT'){
		e.value+=txt;
		$b(e);
	}else{
		e.innerHTML+=txt;
		if(e.contentEditable=='true')
			$b(e);
	}
}
//$l=function(){if(ui.keys)document.onkeyup=ui.onkey;}
$a=function(eid,a,v){$(eid).setAttribute(a,v);}
$r=function(ev,ths,axpb){if(!ev)ev=window.event;$b(ths);if(ev.keyCode!=13)return true;$x(axpb);return false;}
$f=function(eid){
	const e=$(eid);
	if(!e)return;
	if(e.focus)e.focus();
	if(e.setSelectionRange)e.setSelectionRange(e.value.length,e.value.length);
	/*if(e.select)e.select();*/
}
$t=function(s){document.title=s;}
ui.alert=function(msg){alert(msg);}
ui._clnfldvl=function(s){return s.replace(/\r\n/g,'\n').replace(/\r/g,'\n');}
ui._hashKey=function(event){
	const kc=(event.shiftKey?'s':'')+(event.ctrlKey?'c':'')+(event.altKey?'a':'')+(event.metaKey?'m':'')+String.fromCharCode(event.keyCode);
	$d(kc);
	return kc;
}
ui.scrollToTop=function(){window.scrollTo({top:0,behavior:'smooth'});}
ui.keys=[];
ui.onkey=function(ev){
	if(!ev)ev=window.event;
	const cmd=ui.keys[ui._hashKey(ev)];
	if(cmd)eval(cmd);
}
ui.fmtsize=function(num){
	return num.toString().replace(/\B(?=(\d{3})+\b)/g,',');
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
ui.qpb=function(e){
	if(ui.is_dbg_pb)$d('qpb '+e.id);
	if(ui.qpbhas(e.id))return;
	ui._pbls[e.id]=e.id;
}
$b=ui.qpb;
ui.qpbhas=function(id){return id in ui._pbls;}
ui._axc=0;
$x=function(pb){
	if(ui.is_busy){
		ui.alert('Server is busy. Try again in a moment.');
		return;
	}
	if(!ui.is_open){
		ui.alert('Connection to server is not open.');
		return;
	}
	let post='$='+pb+'\r';
	for(const id in ui._pbls){
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
	ui._pbls=[];

	ui._axc++;
	$d('\nmessage #'+ui._axc+': '+post.length+' B');
	$d('~~~~~~~ ~~~~~~~ ~~~~~~~ ~~~~~~~ ')
	$d(post.replace('\r','\n'));
	$d('~~~~~~~ ~~~~~~~ ~~~~~~~ ~~~~~~~ ');
	ui.is_busy=true;
	ui.ws.send(post);
}

ui.ws=new WebSocket('ws'+(location.protocol=='https:'?'s':'')+'://'+location.host+'/bob/websocket');
ui.ws.onopen=function(e){
	$d('web socket: onopen');
	ui.is_open=true;
}
ui.ws.onmessage=function(e){
	$d('response: '+e.data.length+' B');
	$d(e.data);
	if(e.data.length>0)
		eval(e.data);
	ui.is_busy=false;
	$d('~~~~~~~ ~~~~~~~ ~~~~~~~ ~~~~~~~ ')
};
ui.ws.onerror=function(e){
	$d('web socket: onerror '+e+' data:'+e.data);
	ui.alert('An error has occured while communicating with the server.')
};
ui.ws.onclose=function(e){
	$d('web socket: onclose '+e.reason);
	ui.is_open=false;
	if(e.wasClean){
		ui.alert('Connection to server closed.');
	}else{
		ui.alert('Connection to server lost.');
	}
};
setInterval(()=>{
	if(ui.is_busy||!ui.is_open)
		return;
	console.log('sending keep alive');
	ui.ws.send('');
},5*60*1000);
