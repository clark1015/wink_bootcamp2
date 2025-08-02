package hello.wink_bootcamp.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String email, String code) {
        log.info("이메일 전송 요청: {} 에게 인증 코드 발송", email);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("🎯 WinkBootCamp 이메일 인증");
            helper.setText(createEmailTemplate(code), true);

            mailSender.send(message);
            log.info("이메일 전송 성공: {}", email);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {} - {}", email, e.getMessage());
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }

    private String createEmailTemplate(String code) {
        return """
                <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>WinkBootCamp 이메일 인증</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333333;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        padding: 20px;
                    }
                    
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: #ffffff;
                        border-radius: 20px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                        overflow: hidden;
                        position: relative;
                    }
                    
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        padding: 40px 30px;
                        text-align: center;
                        position: relative;
                        overflow: hidden;
                    }
                    
                    .header::before {
                        content: '';
                        position: absolute;
                        top: -50%;
                        left: -50%;
                        width: 200%;
                        height: 200%;
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="20" cy="20" r="2" fill="rgba(255,255,255,0.1)"/><circle cx="80" cy="80" r="2" fill="rgba(255,255,255,0.1)"/><circle cx="40" cy="60" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="60" cy="30" r="1.5" fill="rgba(255,255,255,0.1)"/></svg>');
                        animation: float 20s infinite linear;
                    }
                    
                    @keyframes float {
                        0% { transform: translate(0%, 0%) rotate(0deg); }
                        100% { transform: translate(10%, 10%) rotate(360deg); }
                    }
                    
                    .logo {
                        font-size: 32px;
                        font-weight: 800;
                        color: #ffffff;
                        margin-bottom: 10px;
                        position: relative;
                        z-index: 1;
                    }
                    
                    .subtitle {
                        color: rgba(255,255,255,0.9);
                        font-size: 16px;
                        position: relative;
                        z-index: 1;
                    }
                    
                    .content {
                        padding: 50px 40px;
                        text-align: center;
                    }
                    
                    .title {
                        font-size: 28px;
                        font-weight: 700;
                        color: #333333;
                        margin-bottom: 20px;
                    }
                    
                    .description {
                        font-size: 16px;
                        color: #666666;
                        margin-bottom: 40px;
                        line-height: 1.8;
                    }
                    
                    .code-container {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        border-radius: 16px;
                        padding: 30px;
                        margin: 40px 0;
                        position: relative;
                        overflow: hidden;
                    }
                    
                    .code-container::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="0.5"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
                    }
                    
                    .code-label {
                        color: rgba(255,255,255,0.9);
                        font-size: 14px;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                        margin-bottom: 15px;
                        position: relative;
                        z-index: 1;
                    }
                    
                    .verification-code {
                        font-size: 42px;
                        font-weight: 800;
                        color: #ffffff;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                        text-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        position: relative;
                        z-index: 1;
                    }
                    
                    .warning {
                        background: #fff3cd;
                        border: 1px solid #ffeaa7;
                        border-radius: 12px;
                        padding: 20px;
                        margin: 30px 0;
                        text-align: left;
                    }
                    
                    .warning-title {
                        font-weight: 600;
                        color: #856404;
                        margin-bottom: 8px;
                        font-size: 14px;
                    }
                    
                    .warning-text {
                        color: #856404;
                        font-size: 14px;
                        line-height: 1.6;
                    }
                    
                    .footer {
                        background: #f8f9fa;
                        padding: 30px 40px;
                        text-align: center;
                        border-top: 1px solid #e9ecef;
                    }
                    
                    .footer-text {
                        color: #6c757d;
                        font-size: 14px;
                        line-height: 1.6;
                    }
                    
                    .brand {
                        color: #667eea;
                        font-weight: 600;
                        text-decoration: none;
                    }
                    
                    @media (max-width: 600px) {
                        .container {
                            margin: 0;
                            border-radius: 0;
                        }
                        
                        .content {
                            padding: 30px 20px;
                        }
                        
                        .header {
                            padding: 30px 20px;
                        }
                        
                        .verification-code {
                            font-size: 32px;
                            letter-spacing: 4px;
                        }
                        
                        .footer {
                            padding: 20px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🚀 WinkBootCamp</div>
                        <div class="subtitle">당신의 성장을 응원합니다</div>
                    </div>
                    
                    <div class="content">
                        <h1 class="title">이메일 인증이 필요해요! 📧</h1>
                        <p class="description">
                            안전한 계정 생성을 위해 아래 인증번호를 입력해주세요.<br>
                            이 코드는 <strong>5분간</strong> 유효합니다.
                        </p>
                        
                        <div class="code-container">
                            <div class="code-label">인증번호</div>
                            <div class="verification-code">
                """ + code + """
                            </div>
                        </div>
                       \s
                        <div class="warning">
                            <div class="warning-title">🔐 보안 안내</div>
                            <div class="warning-text">
                                • 이 코드를 다른 사람과 공유하지 마세요<br>
                                • 본인이 요청하지 않았다면 이 이메일을 무시하세요<br>
                                • 문제가 있다면 고객센터로 문의해주세요
                            </div>
                        </div>
                    </div>
                    <div class="footer">
                        <p class="footer-text">
                            이 이메일은 <a href="#" class="brand">WinkBootCamp</a>에서 발송되었습니다.<br>
                            궁금한 점이 있으시면 언제든 연락주세요! 💌
                        </p>
                    </div>
                </div>
            </body>
            </html>
               \s""";
    }
}