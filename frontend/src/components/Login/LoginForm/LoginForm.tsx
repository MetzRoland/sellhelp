// import { Link } from "react-router";
// import type { InputComponentType } from "../../Reusables/InputComponent/InputComponentTypes";
// import type { InputFormType } from "../../Reusables/InputForm/InputFormTypes";
// import type { User } from "../../../contextProviders/AuthProvider/AuthProviderTypes";
// import { useLoading } from "../../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
// import InputForm from "../../Reusables/InputForm/InputForm";
// import type { LoginForm } from "../LoginTypes";

// export interface LoginFormProps extends InputComponentType, InputFormType {
//   formSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
//   handleGoogleLogin: () => void;
//   authError: string;
//   user: User;
// }

// function LoginForm({
//     errorMessage, 
//     inputType, 
//     inputName, 
//     inputValue, 
//     inputPlaceholder, 
//     handleFunction,
//     formData,
//     inputs,
//     formSubmit,
//     options,
//     handleGoogleLogin,
//     authError,
//     user
// }: LoginFormProps){

//     const {setIsLoading} = useLoading();

//     return (
//         <>
//             <h1 className="container-title">Bejelentkezés</h1>

//             <form
//               className="content-container login-form"
//               onSubmit={formSubmit}
//             >
//               <InputForm<LoginForm>
//                 inputs={inputs}
//                 formData={formData}
//                 handleFunction={handleFunction}
//                 errorMessage={errorMessage}
//               />

//               <button className="btn border-btn" type="submit">
//                 Bejelentkezés
//               </button>

//               <button
//                 className="btn border-btn"
//                 type="button"
//                 onClick={() => {
//                   setIsLoading(true);
//                   handleGoogleLogin();
//                   setIsLoading(false);
//                 }}
//               >
//                 Folytatás Google‑val
//               </button>

//               {authError && (
//                 <p className="message error error-process-status">
//                   {authError}
//                 </p>
//               )}

//               {user && (
//                 <p className="message success-message">
//                   Sikeres Bejelentkezés!
//                 </p>
//               )}
//             </form>

//             <div className="content-container back-to-register-container">
//               <h2>Nincs még fiókod?</h2>
//               <Link to="/register" className="btn">
//                 Regisztráció
//               </Link>
//             </div>
//           </>
//     );
// }

// export default LoginForm;