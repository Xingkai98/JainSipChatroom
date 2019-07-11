package cn.sse;
import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
	private static String UPDATE_NOTICE = "UPD";
	private static String ALL_NOTICE = "ALL";
	private static String SOME_NOTICE = "SOM";
	private static String P2P_NOTICE = "P2P";
	
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
	
	void copyToAll(String s) {
		try {
			for(int j=0;j<clientList.size();j++) {
				sipLayer.sendMessage(clientList.get(j), s);
			}
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
	@Override
	public void processReceivedMessage(String sender, String message) {
		// TODO Auto-generated method stub
		//�յ���Ϣ
		String senderSipAddress = getSipAddress(sender);
		System.out.println("[Received message from " + sender + "]: " + message);
		//System.out.println(senderSipAddress);
		
		
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
			String onlineListString = UPDATE_NOTICE + "&";
			int i=0;
			while(i<clientList.size()-1) {
				onlineListString += clientList.get(i) + "&";
				i++;
			}
			onlineListString += clientList.get(clientList.size()-1);
			copyToAll(onlineListString);
		}
		
		//�������յ��ͻ��˷�������Ϣlist�������ߵ�ַ&��ַ1&��ַ2&��Ϣ���ݡ�
		//��δָ���ռ��ˣ�����ϢΪ�����ߵ�ַ&��Ϣ���ݡ�
		//��������ͻ���ת��������Ϣ���ͣ�ȫ�塢˽�ġ����֣�&��Ϣ����&��Ϣ���ݡ�
		else {
			String[] list = message.split("&");
			//���ͻ��˷�������Ϣlist����Ϊ2�������ߵ�ַ&��Ϣ���ݡ�����˵����Ⱥ��
			//��������ת����ʽΪ ALL_NOTICE&���ߵ�ַ&��Ϣ����
			//���ͻ��˷�������Ϣlist����Ϊ3��˵����˽������Ϊֻ��һ��������
			String toSend = "";
			if(list.length == 2) {
				toSend = ALL_NOTICE + "&" + list[0] + "&" + list[list.length-1];
				copyToAll(toSend);
				return;
			}
			else if(list.length == 3) {
				toSend = P2P_NOTICE + "&" + list[0] + "&" + list[list.length-1];
				try {
					sipLayer.sendMessage(list[1], toSend);
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
				//copyToAll(toSend);
				return;
			}
			else {
				toSend = SOME_NOTICE + "&" + list[0] + "&" + list[list.length-1];
				//���������յ�����ϢΪ�����ߵ�ַ&��ַ1&��ַ2...&��Ϣ���ݡ�������Ŀ���ַ��1��length-2���±�����
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
	public static void main(String[] args) throws Exception {
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
		System.out.println("local ip: " + InetAddress.getLocalHost().getHostAddress());
		
		//��Ӽ���
		sipLayer.addSipMessageListener(server);
	}
}
