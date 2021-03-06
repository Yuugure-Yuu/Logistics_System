package businesslogic.logisticsbl;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import po.agency.StaffPO;
import po.list.DeliveringListPO;
import po.list.HallArrivalListPO;
import po.list.LoadListPO;
import po.list.OrderListPO;
import po.repertory.GoodsInfoPO;
import po.system.SystemLogPO;
import presentation.mainui.CurrentUser;
import dataservice.agency.AgencyDataService;
import dataservice.agency.StaffDataService;
import dataservice.list.DeliveringListDataService;
import dataservice.list.HallArrivalListDataService;
import dataservice.list.LoadListDataService;
import dataservice.list.OrderListDataService;
import dataservice.system.SystemLogDataService;
import utility.ResultMessage;
import vo.DeliveringListVO;
import vo.GoodsInfoVO;
import vo.HallArrivalListVO;
import vo.TransArrivalVO;
import businesslogic.rmi.RMIHelper;
import businesslogicservice.logisticsblservice.DeliverAndReceiveBLService;

public class DeliverAndReceiveBLImpl implements DeliverAndReceiveBLService {
	HallArrivalListDataService service1=null;
	DeliveringListDataService service2=null;
	OrderListDataService service3=null;
	StaffDataService staff=null;
	CurrentUser user=null;
	SystemLogDataService system=null;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	LoadListDataService loadlistdataservice=null;
	AgencyDataService agency=null;
	
	public DeliverAndReceiveBLImpl(CurrentUser currentuser){
		staff=(StaffDataService) RMIHelper.find("StaffDataService");
		service1=(HallArrivalListDataService)RMIHelper.find("HallArrivalListDataService");
		service2=(DeliveringListDataService)RMIHelper.find("DeliveringListDataService");
		service3=(OrderListDataService)RMIHelper.find("OrderListDataService");
		user=currentuser;
		system=(SystemLogDataService)RMIHelper.find("SystemLogDataService");
		loadlistdataservice=(LoadListDataService)RMIHelper.find("LoadListDataService");
		agency=(AgencyDataService)RMIHelper.find("AgencyDataService");
	}
	public ResultMessage createHallArrivalList(HallArrivalListVO hallArrivalList) {
		// TODO Auto-generated method stub
		HallArrivalListPO hallarrivallist=null;
		ArrayList<GoodsInfoVO> goodsvolist=hallArrivalList.getGoodsInfoVO();
		ArrayList<GoodsInfoPO> goodspolist=new ArrayList<GoodsInfoPO>();
		for(GoodsInfoVO goodsvo:goodsvolist){
			try {
				goodspolist.add(new GoodsInfoPO(goodsvo.getBarcode(),goodsvo.getState()));
				OrderListPO orderListPO=service3.find(goodsvo.getBarcode());
				ArrayList<String> orderpath=orderListPO.getPkgState();
				orderpath.add((String)df.format(new Date())+" ????????????"+user.getAgencyName());
				orderListPO.setPkgState(orderpath);
				service3.update(orderListPO);
			} catch (RemoteException e) {
				e.printStackTrace();
			}		
		}

		hallarrivallist=new HallArrivalListPO(hallArrivalList.getId(),hallArrivalList.getDate(),hallArrivalList.getTransferNumber(),hallArrivalList.getDepartureplace(),goodspolist,hallArrivalList.getCheckType());
		try {
			service1.add(hallarrivallist);
			system.add(new SystemLogPO((String)df.format(new Date()),"????????????????????????,?????????"+hallarrivallist.getId(),user.getAdmin()));
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
		return new ResultMessage(true, "??????????????????????????????!");
	}
	public ResultMessage createDeliveringList(DeliveringListVO deliveringList) {
		DeliveringListPO deliverlistpo=null;
		for(String id:deliveringList.getBarCode()){
			try {
				OrderListPO orderListPO=service3.find(id);
				ArrayList<String> orderpath=orderListPO.getPkgState();
				ArrayList<StaffPO> stafflist=staff.findbyname(deliveringList.getDeliveryMan());
				StaffPO st=null;
				for(int i=0;i<stafflist.size();i++){
					if(stafflist.get(i).getAgencyId().equals(deliveringList.getId().substring(0, 6)))
						st=stafflist.get(i);			
				}
				if(st==null)
					return  new ResultMessage(false, "???????????????????????????!");
				orderpath.add((String)df.format(new Date())+" ????????????????????????"+deliveringList.getDeliveryMan()+"??????,?????? "+st.getPhoneNum());
				orderListPO.setPkgState(orderpath);
				service3.update(orderListPO);
			} catch (RemoteException e) {
				e.printStackTrace();
			}	
		}
		deliverlistpo=new DeliveringListPO(deliveringList.getId(),deliveringList.getDate(),deliveringList.getBarCode(),deliveringList.getDeliveryMan(),deliveringList.getCheckType());
		try {
			service2.add(deliverlistpo);
			system.add(new SystemLogPO((String)df.format(new Date()),"???????????????,?????????"+deliverlistpo.getId(),user.getAdmin()));
		} catch (RemoteException e) {
			e.printStackTrace();		
		}
		return new ResultMessage(true, "?????????????????????!");
	}
	public String createHallArrivalListId() {
		// TODO ???????????????????????????
		String s="";
		try{
		s=(service1.showAllByAgency(user.getAgencyNum()).size()+1)+"";
		}catch(RemoteException e){
			e.printStackTrace();
		}
		int num=s.length();
		for(int i=0;i<5-num;i++)
			s="0"+s;
		return user.getAgencyNum()+s;
	}
	public String createDeliveringListId() {
		// TODO ???????????????????????????
		String s="";
		try{
		s=(service2.showAllbyAgency(user.getAgencyNum()).size()+1)+"";
		}catch(RemoteException e){
			e.printStackTrace();
		}
		int num=s.length();
		for(int i=0;i<5-num;i++)
			s="0"+s;
		return user.getAgencyNum()+s;
	}
	public TransArrivalVO getLoadList(String loadlistid) {
		// TODO ???????????????????????????
		LoadListPO loadlistpo=null;
		try{
			loadlistpo=loadlistdataservice.find(loadlistid);
			if(loadlistpo==null)
				return null;
			else{
				TransArrivalVO transvo=new TransArrivalVO();
				transvo.barcodes=loadlistpo.getBarcodes();
				transvo.depatureplace=agency.find(loadlistpo.getHallNumber()).getAgencyName();
				return transvo;
			}
			}catch(RemoteException e){
				e.printStackTrace();
			}
		return null;
	}
	
}
