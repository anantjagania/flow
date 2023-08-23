package com.vaadin.flow.uitest.ui.servlets;

import jakarta.servlet.annotation.WebServlet;

import com.vaadin.flow.server.VaadinServlet;

@WebServlet("/*")
public class WorkaroundServlet extends VaadinServlet {

}
