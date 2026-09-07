$(document)
.on('click','#btnSubmit',function(){
	$.ajax({
		url:'/updateBySelf',type:'post',dataType:'text',
		data:$('#frmPersonal').serialize(),
		beforeSend:function(){
			if($.trim($('#name').val())==''){
				alert('실명(이름)을 입력하시오'); return false;
			}
			if($('input[name=gender]:checked').val()===undefined){
				alert('성별을 선택하시오');
				return false;
			}
			if($('#email').val()==''){
				alert('이메일주소를 입력하십시오. 비밀번호 확인과 모바일번호의 본인확인에 사용될 수 있습니다.'); return false;
			}
			if($('#passcode').val()==''){
				alert('본인 확인을 위한 (로그인)비밀번호확인을 입력하십시오.'); return false;
			}
			console.log(this.data);
		},
		success:function(data){
			if(data=='1') {
				alert('변경 성공')
				document.location="/";
			} else {
				alert('변경 실패');
			}
		}
	})
	return true;
})
.on('click','#btnAddress',function(){
    new daum.Postcode({
        oncomplete: function(data) {
			console.log(data);
            $('#address').val(data.address).focus();
        }
    }).open();
    return false;
})