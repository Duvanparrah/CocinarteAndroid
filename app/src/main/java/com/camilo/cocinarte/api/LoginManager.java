package com.camilo.cocinarte.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.Usuario;

public class LoginManager {
    private SharedPreferences prefs;

    public LoginManager(Context context) {
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString("token", token).apply();
    }

    public void saveUser(LoginResponse.User usuario) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("id_usuario", String.valueOf(usuario.getId()));
        editor.putString("correo", usuario.getEmail());
        editor.putString("tipo_usuario", usuario.getTipo_usuario());
        editor.putString("nombre_usuario", usuario.getNombre());
        editor.putString("foto_perfil", usuario.getFoto());
        editor.apply();
    }

    public Usuario getUsuario() {
        String id = prefs.getString("id_usuario", null);
        String correo = prefs.getString("correo", null);
        String tipoUsuario = prefs.getString("tipo_usuario", null);
        String nombreUsuario = prefs.getString("nombre_usuario", null);
        String fotoPerfil = prefs.getString("foto_perfil", null);

        if (id == null || correo == null) return null;

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(Integer.parseInt(id));
        usuario.setCorreo(correo);
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setFotoPerfil(fotoPerfil);

        return usuario;
    }


    public String getToken() {
        return prefs.getString("token", null);
    }



    public void clear() {
        prefs.edit().clear().apply();
    }
}