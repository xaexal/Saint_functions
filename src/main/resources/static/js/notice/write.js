$(document)
.ready(()=>{
	$.ajaxSetup({type:'post',dataType:'json'});
})
.on('click','#btnUpdate',()=>{
	document.location='/notice/update'+'?id='+$('#id').val();
	return false;
})
.on('click','#btnDelete',()=>{
	$.ajax({url:'/notice/delete',data:{id:$('#id').val()},
		beforeSend:function(){
			if(!confirm('이 게시물을 삭제할까요?')) return false;
		},
		error:function(xhr,status,error){
			alert('오류번호 ['+status+'] '+error);
		},
		success:function(result){
			console.log(result);
			alert(result['message']);
			if(result['errcode']==cNone){
				document.location=result['data'];
			} else {
				if(result['errcode']==cSignin) document.location=result['data'];
			}
		}
	})
	return false;
})
.on('click','#btnDone',function(){
	$.ajax({url:'/notice/save',
		data:{member_id:$('#member_id').val(),title:$('#title').val(),content:$('#content').val(),
		level:$('#level').val(),id:$('#id').val()},
		beforeSend:function(){
			$('#title').val($.trim($('#title').val()));
			if($('#title').val()=='') {
				alert('제목을 입력하십시오'); return false;
			}
			if($('#content').val()==''){
				alert('내용을 입력하십시오'); return false;
			}
		},
		error:function(xhr,status,error){
		 	alert('오류번호 ['+status+'] '+error);
		},
		success:function(result){
			console.log(result);
			if(result['errcode']!=cNone){
				alert(result['message']);
				if(result['errcode']==cSignin) document.location=result['data'];
				return false;
			}
			if(!confirm("변경됐습니다.\n작성된 내용을 확인할까요?\n아니면, 목록보기로 돌아갑니다.")) {
				document.location="/";
			} else {
				document.location="/notice/view?id="+result['data'];
			}
			return false;
		}
	});
	return false;
})