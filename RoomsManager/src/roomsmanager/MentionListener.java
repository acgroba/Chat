package roomsmanager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionListener implements MessageListener {
    private static Pattern pattern = Pattern.compile("@\\S+");
    private final MessageSender messageSender;
    private final String roomName;

    MentionListener(MessageSender messageSender, String roomName) {
        this.messageSender = messageSender;
        this.roomName = roomName;
    }

    @Override
    public void onMessage(Message msg) {
            if (msg instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) msg;

                // search for mentions in message
                Matcher matcher = null;
                try {
                    matcher = pattern.matcher(txtMsg.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                List<String> mentions = new LinkedList<>();
                while (matcher != null && matcher.find()) {
                  
                    mentions.add(matcher.group());
                }
                if(!mentions.isEmpty()){
                    for (String user : mentions) {
                         try {
                        this.messageSender.sendMention(user.substring(1), roomName,txtMsg.getText());
                         } catch (JMSException e) {
                    e.printStackTrace();
                }
                    }
                }
            }
    }
}
