package com.camilo.cocinarte.models;

public class UpdatePhotoResponse {
        private String message;
        private String profileImageUrl;
        private User user;

        public String getMessage() {
            return message;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public User getUser() {
            return user;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public static class User {
            private int id;
            private String email;
            private String nombre;
            private String foto_perfil;
            private boolean isVerified;
            private String tipo_usuario;

            public int getId() {
                return id;
            }

            public String getEmail() {
                return email;
            }

            public String getNombre() {
                return nombre;
            }

            public String getFoto_perfil() {
                return foto_perfil;
            }

            public boolean isVerified() {
                return isVerified;
            }

            public String getTipo_usuario() {
                return tipo_usuario;
            }

            public void setId(int id) {
                this.id = id;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public void setNombre(String nombre) {
                this.nombre = nombre;
            }

            public void setFoto_perfil(String foto_perfil) {
                this.foto_perfil = foto_perfil;
            }

            public void setVerified(boolean verified) {
                isVerified = verified;
            }

            public void setTipo_usuario(String tipo_usuario) {
                this.tipo_usuario = tipo_usuario;
            }
        }
    }
