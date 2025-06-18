package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class UsuarioUpdateRequest {

    @SerializedName("correo")
    private String correo;

    @SerializedName("nombre_usuario")
    private String nombreUsuario;

    @SerializedName("tipo_usuario")
    private String tipoUsuario;

    @SerializedName("isVerified")
    private Boolean isVerified;

    @SerializedName("foto_perfil")
    private String fotoPerfil;

    public UsuarioUpdateRequest(String correo, String nombreUsuario, String tipoUsuario, Boolean isVerified, String fotoPerfil) {
        this.correo = correo;
        this.nombreUsuario = nombreUsuario;
        this.tipoUsuario = tipoUsuario;
        this.isVerified = isVerified;
        this.fotoPerfil = fotoPerfil;
    }
}
