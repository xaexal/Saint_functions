//const cNone = '0';
/*const cError = '-1';
const cSignin = '-2';
const cSignup = '-3';*/

function dayCount(cid,labelID){
	$.post('/common/countHoliday',{cid:cid},function(data){
		console.log('countHoliday',data)
		if(data['result']!='0'){
			alert(data['msg']); return false;
		}
		let period1=data['period1'];
		let period2=data['period2'];
		let start=new Date(parseInt(period1.substring(0,4)),parseInt(period1.substring(4,6))-1,
							parseInt(period1.substring(6,8)))
		let end = new Date(parseInt(period2.substring(0,4)),parseInt(period2.substring(4,6))-1,
							parseInt(period2.substring(6,8)))
		console.log(start); console.log(end)
		let past=0;
		let cnt=0;
		while(start<=end){
			let countable=true;
			if(start.getDay()==0 || start.getDay()==6){
				countable=false;
			} else {
				$.each(data['rec'],function(ndx,holiday){
//					console.log(holiday, strDate(start))
					if(holiday['holidate']==strDate(start)){
						countable=false;
						return false;
					}
				})
			}
//			console.log(strDate(start),countable, start.getDay())
			if(countable) cnt++;
			if(strDate(start)==strDate(new Date())) past=cnt;
			start=new Date(start.getFullYear(),start.getMonth(),start.getDate()+1)
		}
		console.log('holdays=',cnt)
		labelID.text(past+'/'+cnt)
	},'json')
}


function strDate(someday){
	let pstr= ''+someday.getFullYear();
	let month=someday.getMonth()+1;
	if(month<10) pstr+='0';
	pstr+=month;
	let n=someday.getDate();
	if(n<10) pstr+='0';
	pstr+=n;
	return pstr;
}