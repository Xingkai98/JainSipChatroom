package cn.sse;
import java.io.BufferedReader;
import java.text.ParseException;
import java.util.*;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;

public class Server implements SipMessageListener{
	private static SipLayerFacade sipLayer;
	//������list�洢����sip��ַ
	private static List<String> clientList;
	private static List<String> toSendList;
	private static String grantPermission = "********";
	private static String updateOnlineList = "&&&&&&&&";
	private static String updateNotice = "UPD";
	private static String AllNotice = "ALL";
	private static String SomeNotice = "SOM";
	private static String P2pNotice = "P2P";
	
	public Server() {
		clientList = new ArrayList<String>();
		toSendList = new ArrayList<String>();
	}
	
	//��ȡ�ͻ��б�
	public List<String> getClientList(){
		return clientList;
	}
	
	//�ӷ�������Ϣ�����sip��ַ����"Alias" <sip:Alias@127.0.0.1:8090>
	public String getSipAddress(String sender) {
		String temp = sender.substring(sender.indexOf('<'), sender.indexOf('>'));
		temp = temp.substring(temp.indexOf(':')+1);
		return temp;
	}
	
	@Override
	public void processReceivedMessage(String sender, String message) {
		// TODO Auto-generated method stub
		//�յ���Ϣ
		String senderSipAddress = getSipAddress(sender);
		System.out.println("[Received message from " + sender + "]: " + message);
		//System.out.println(senderSipAddress);
		
		//�ͻ�������������ͣ������ߵ�ַ&��ַ1&��ַ2&��Ϣ���ݡ�
		//�������յ���Ϣ�����̸���Щ��ַת����Ϣ���ݡ�
		//��������ͻ���ת��������Ϣ���ͣ�ȫ�塢˽�ġ����֣�&��Ϣ����&��Ϣ���ݡ�
		
		//��Ϊ�µ�½��Ա������ϢΪgrantPermission������������б�
		boolean isNew = true;
		for(int i=0;i<clientList.size();i++) {
			if(clientList.get(i).equals(senderSipAddress)) {
				isNew = false;
			}
		}
		System.out.println("isNew: "+ isNew);
		//�������������б�������������û�����Ϣ���������б�
		if(isNew) {
			clientList.add(senderSipAddress);
			String onlineListString = updateNotice + "&";
			int i=0;
			while(i<clientList.size()-1) {
				onlineListString += clientList.get(i) + "&";
				i++;
			}
			onlineListString += clientList.get(clientList.size()-1);
			for(int j=0;j<clientList.size();j++) {
				try {
					sipLayer.sendMessage(clientList.get(j), onlineListString);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//�����³�Ա�����ո�ʽת����Ϣ
		//��������ͻ���ת��������Ϣ���ͣ�ȫ�塢˽�ġ����֣�&��Ϣ����&��Ϣ���ݡ�
		//��Ĭ��Ϊȫ��
		else {
			String[] list = message.split("&");
			String toSend = AllNotice + "&" + list[0] + "&" + list[list.length-1];
			
			//�ͻ�������������ͣ������ߵ�ַ&��ַ1&��ַ2&��Ϣ���ݡ�������Ŀ���ַ��1��length-2���±�����
			for(int i=1;i<list.length-1;i++) {
				try {
					sipLayer.sendMessage(list[i], toSend);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	@Override
	public void processError(String errorMessage) {
		// TODO Auto-generated method stub
		System.out.println("Server error.");
	}
	@Override
	public void processInfo(String infoMessage) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		try {
		    String username = "Server";
		    String hostIP = "127.0.0.1";
		    int port = 8080;   
			sipLayer = new SipLayerFacade(username, hostIP, port);		
        } catch (Throwable e) {
            System.out.println("Problem initializing the SIP stack.");
            e.printStackTrace();
            System.exit(-1);
        }
		Server server = new Server();
		System.out.println("Server started.");
		
		//��Ӽ���
		sipLayer.addSipMessageListener(server);
	}
}
