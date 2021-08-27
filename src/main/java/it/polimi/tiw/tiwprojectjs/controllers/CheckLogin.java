package it.polimi.tiw.tiwprojectjs.controllers;

import it.polimi.tiw.tiwprojectjs.beans.User;
import it.polimi.tiw.tiwprojectjs.dao.UserDAO;
import it.polimi.tiw.tiwprojectjs.exception.BadLoginException;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "CheckLogin", value = "/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {

    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params:
     * email -> required, max 100
     * user-password -> required, max 45
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String email = null;
        String password = null;
        String errorMessage = null;

        // get parameters from request
        try{

            email = StringEscapeUtils.escapeJava(request.getParameter("email"));
            password = StringEscapeUtils.escapeJava(request.getParameter("user-password"));

            if (email == null ){

                errorMessage = "Bad email: email is null";
                throw new BadLoginException(errorMessage);
            } else if ( email.isEmpty() ){

                errorMessage = "Bad email: email is empty";
                throw new BadLoginException(errorMessage);
            }else if ( !email.contains("@") ){

                errorMessage = "Bad email: email must contain @ ";
                throw new BadLoginException(errorMessage);
            } else if ( password == null  ){

                errorMessage = "Bad Password: password is null";
                throw new BadLoginException(errorMessage);
            } else if (password.isEmpty()){

                errorMessage = "Bad Password: password is empty";
                throw new BadLoginException(errorMessage);
            }

        } catch ( BadLoginException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        UserDAO userDAO = new UserDAO(connection);
        User user = null;

        try {

            user = userDAO.checkCredentials(email,password);
        } catch ( SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Failed to retrieve user");
            return;
        } catch ( BadLoginException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        if (user == null){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("User not found");
            return;
        } else {

            // sets the user as a session attribute
            request.getSession().setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(user.getUsername());
        }
    }

    public void destroy(){

        try{

            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e){

            e.printStackTrace();
        }
    }
}
