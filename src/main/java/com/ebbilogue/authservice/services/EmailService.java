package com.ebbilogue.authservice.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String to, String code, String username) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("code", code);
        
        //Thymeleaf 模板引擎默认会在 src/main/resources/templates 目录下查找模板文件。
        String emailContent = templateEngine.process("password-reset-email-en", context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("密码重置请求");
        helper.setText(emailContent, true);  // true表示使用HTML格式

        mailSender.send(message);
    }
}