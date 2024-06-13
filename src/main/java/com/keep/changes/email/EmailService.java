package com.keep.changes.email;

public interface EmailService {

	void sendEmail(String to, String username, EmailTemplateName emailTemplate, String subject);
}
