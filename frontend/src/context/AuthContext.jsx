import { createContext, useContext, useState, useEffect } from "react";
import apiClient from '../services/apiClient';
import { id } from "date-fns/locale";

const AuthContext = createContext(null);


export function AuthProvider({children}){

    const[user, setUser] = useState(null);
    const[loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if(token){
            try{
                const payload = JSON.parse(atob(token.split('.')[1]));

                if(payload.exp * 1000 > Date.now()){
                    setUser({
                        id: payload.sub,
                        email: payload.email,
                        name: payload.name,
                        role: payload.role,
                    });
                } else {
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('refreshToken');
                }
            } catch {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
            }
        }
        setLoading(false);
    },[]);

    const login = async(email, password) => {
      const { data } = await apiClient.post('/auth/login', {email, password});

      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);

      setUser(data.user);
      return data;
    };

    const register = async(email, password, name) => {
      const {data} = await apiClient.post('/auth/register', {email, password, name});
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);

      setUser(data.user);
      return data;
    };

    const logout = () => {
      const rt = localStorage.getItem('refreshToken');
      if(rt){
        apiClient.post('/auth/logout', {refreshToken : rt}).catch(() => {});
      }

      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setUser(null);
    };

    const isAdmin = () => user?.role === 'ADMIN';

    return(
      <AuthContext.Provider value={{ user, login, register, logout, loading, isAdmin}}>
        {children}
      </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);