package com.adobe.aem.mysite.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.commons.mail.MailTemplate;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import org.apache.commons.mail.HtmlEmail;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;

@Component(service = WorkflowProcess.class,
           property = {"process.label = Review Process Email Notification"}
)
public class ReviewProcessWorkflow implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(ReviewProcessWorkflow.class);
    private static final String PUBLISH_EMAIL_TEMPLATE = "/content/dam/mysite/emailTemplates/pagePublishedEmailTemplate.html";
    private static final String EMAIL_SUBJECT = "Page is reviewed and published";
    private static final String EMAIL_RECIPIENT = "nagarajmoonraft@gmail.com";
    private static final String DOMAIN = "http://localhost:4502/";

    @Reference
    MessageGatewayService messageGatewayService;

    @Override
    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {

        WorkflowData workflowData = item.getWorkflowData();
        String payloadPath = workflowData.getPayload().toString();
        log.info("payloadPath : "+ payloadPath);
        ResourceResolver resolver = session.adaptTo(ResourceResolver.class);


        try{
            if(payloadPath != null){
                String pageLink = DOMAIN + payloadPath + ".html";
                Map<String,String> parameters = new HashMap<>();
                parameters.put("pageLink",pageLink);
                Resource templateResource = resolver.getResource(PUBLISH_EMAIL_TEMPLATE + "/jcr:content");
                if(templateResource != null){
                    Node templateNode = templateResource.adaptTo(Node.class);
                    if(templateNode != null){
                        MailTemplate mailTemplate = MailTemplate.create(PUBLISH_EMAIL_TEMPLATE, templateNode.getSession());
                        HtmlEmail email = mailTemplate.getEmail(parameters, HtmlEmail.class);
                        email.setSubject(EMAIL_SUBJECT);
                        email.addTo(EMAIL_RECIPIENT);
                        MessageGateway<HtmlEmail> messageGateway = messageGatewayService.getGateway(HtmlEmail.class);
                        if(messageGateway != null){
                            messageGateway.send(email);
                        }else {
                            log.error("Error in sending email!!!");
                        }
                    }

                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
