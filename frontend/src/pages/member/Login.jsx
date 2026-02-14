import { useRef } from "react"
import { useUsersContext } from "../../context/useUsersContext";
import { AUTH_KEY, URL, USERS } from "../../config/constants";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function Login(){

    const navigate=useNavigate();

    //<아이디/비번 입력요소 제어용>
    const usernameRef= useRef(null);
    const passwordRef= useRef(null);

    //<<로그인 처리용 함수 받기>>
    const {dispatch} = useUsersContext();

    //<로그인 버튼 이벤트 처리용>>
    const handleLogin = e=>{
        //<button>의 기본적인 기능인 제출(submit)기능 막기
        e.preventDefault();  
        const username=usernameRef.current.value;
        const password=passwordRef.current.value;

        //백엔드 서버로 회원 인증 요청
        axios
        .get(URL.USERS)
        .then(res=>{
            if(res.data.length !==0){//회원이 최소 1명 이상인 경우
            const user=res.data.filter(user=>user.username===username && user.password===password);
            if(user.length===1){//일치하는 회원이 있는 경우
                //1)로그인 처리:세션 스토리지에 'username'키로 사용자 아이디 저장
                sessionStorage.setItem(AUTH_KEY.USERNAME,username);
                //2)로그인 상태(authenticated)를 아이디로 변경
                dispatch({type:USERS.LOGIN,isAuthenticated:username})
                //3)로그인 후 원래 가려던 페이지 혹은 사용자의 상세 프로필로 이동
                //{replace:true}는 사용자가 Back버튼 클릭시 로그인 화면 이동 방지
                // AuthRoute.jsx에서 넘긴 값
                const from = location.state?.from || `/users/${username}`;
                
                //navigate(`/users/${username}`,{replace:true});
                //원래 가려던 페이지로 이동
                navigate(from, { replace: true });
            }
            else
                window.alert('아이디와 비번 불일치');
            }

        })
        .catch(err=>console.log(err));

       
    };


    return <>
        <div className="p-5 bg-warning text-white rounded">
            <h1>
                로그인
            </h1>
        </div>
        <form>
            <div className="row mt-3 d-flex justify-content-center">
                <div className="col-3">
                    <input ref={usernameRef} type="text" className="form-control" placeholder="아이디를 입력하세요" name="username"/>
                </div>
                <div className="col-3">
                    <input ref={passwordRef} type="password" className="form-control" placeholder="비밀번호를 입력하세요" name="password"/>
                </div>
                <div className="col-auto">
                    <button className="btn btn-danger" onClick={handleLogin}>로그인</button>
                </div>
            </div>
        </form>
    
    </>
}