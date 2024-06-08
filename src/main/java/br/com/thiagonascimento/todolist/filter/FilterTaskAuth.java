package br.com.thiagonascimento.todolist.filter;

import br.com.thiagonascimento.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var serveltPath = request.getServletPath();

        if (!serveltPath.startsWith("/tasks/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Pegar a autenticação (usuário e senha)
        var authorization = request.getHeader("Authorization");
        var token = authorization.substring("Basic".length()).trim();
        byte[] authDecode = Base64.getDecoder().decode(token);
        String auth = new String(authDecode);
        String[] credentials = auth.split(":");
        String user = credentials[0];
        String password = credentials[1];

        // Validar usuário
        var userEntity = this.userRepository.findByUsername(user);
        if (userEntity == null) {
            response.sendError(401);
        } else {
            var passwordVerify = BCrypt.checkpw(password, userEntity.getPassword());
            if (!passwordVerify) {
                response.sendError(401);
            }
        }

        request.setAttribute("idUser", userEntity.getId());

        filterChain.doFilter(request, response);
    }
}
