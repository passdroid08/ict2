import { createBrowserRouter } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/member/Login";
import Users from "./pages/member/Users";
import Profile from "./pages/member/Profile";
import Bbs from "./pages/bbs/Bbs";
import Photo from "./pages/photo/Photo";
import NotFound from "./pages/NotFound";
import App from "./App.jsx";
import AuthRoute from "./components/AuthRoute";
import List from "./pages/bbs/List";
import InputForm from "./pages/bbs/InputForm";
import UpdateForm from "./pages/bbs/UpdateForm";
import Detail from "./pages/bbs/Detail";
import ErrorPage from "./components/ErrorPage";
import { authLoader } from "./util/authLoader";
import Result from "./pages/simulator/Result.jsx";
import FormProvider from "./provider/FormProvider.jsx";

/*
    <<<createBrowserRouter를 호출로 BrowserRouter생성>>>
    기존의 <Routes>를 대체
    라우트 설정은 router.jsx 파일로 분리 한다    
    
*/

const router = createBrowserRouter([
    {
        path:'/',
        element:<App/>,
        //<errorElement>속성        
        //라우트 레벨마다 발생한 에러를 처리할수 있다
        //가장 가까운(errorElement가 정의된) 라우트가 우선
        //없으면 부모로 버블링
        //최상위까지 없으면 React Router 에러 화면이 표시된다
        errorElement:<ErrorPage/>, //전체 앱 공통 에러시 보여줄 페이지       
        children:[
            //<index:true속성>
            //path:""보다는 index:true권장
            //부모 경로에 진입시(/bbs) 자동으로 보여주는 기본 자식 페이지
            //path속성과는 함께 쓸수 없다
            {index:true,element:<Home/>},
            {path:"login",element:<Login/>},            
            {
                path:"users",
                element:<Users/>,   
                children:[
                    {path:':username',element:<Profile/>}

                ]            
            },
            
            {  
                element:<AuthRoute/>,
                children:[
                    {
                        path: "bbs",
                        element: <Bbs />, // Bbs 내부에 <Outlet />이 있어야 함
                        children: [
                         
                            { index:true, element: <List /> },
                            { path: "form", element: <InputForm /> },
                            { path: "form/:id",element:<UpdateForm/>},
                            { path: ":id",element:<Detail/>},
                        ]
                    },
                    {
                        path: "result",
                        element: (
                            <FormProvider>
                            <Result />
                            </FormProvider>
                        ), 
                        children: [
                         
                            
                        ]
                    },
                    
                ]
            },
            {
                path:"photo",
                element:<Photo/>,
                //<loader 속성>                
                //컴포넌트가 렌더링 되기 전 실행되는 함수 지정
                //특정 라우트별로 지정 가능          
                loader:authLoader,//라우터별 접근제한시
            },
            {path:"*",element:<NotFound/>},

        ]

    }


]);
export default router;