package com.sunlights.trade.service.impl;

import com.sunlights.common.DictConst;
import com.sunlights.common.utils.DBHelper;
import models.*;

import com.sunlights.account.service.AccountService;
import com.sunlights.account.service.CapitalService;
import com.sunlights.account.service.impl.AccountServiceImpl;
import com.sunlights.account.service.impl.CapitalServiceImpl;
import com.sunlights.core.service.BankCardService;
import com.sunlights.core.service.BankService;
import com.sunlights.core.service.OpenAccountPactService;
import com.sunlights.core.service.ProductService;
import com.sunlights.core.service.impl.BankCardServiceImpl;
import com.sunlights.core.service.impl.BankServiceImpl;
import com.sunlights.core.service.impl.OpenAccountPactServiceImpl;
import com.sunlights.core.service.impl.ProductServiceImpl;
import com.sunlights.core.vo.BankCardVo;
import com.sunlights.customer.service.impl.CustomerService;
import com.sunlights.trade.dal.TradeDao;
import com.sunlights.trade.dal.impl.TradeDaoImpl;
import com.sunlights.trade.service.ShuMiTradeService;
import com.sunlights.trade.vo.ShuMiTradeFormVo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>Project: financeplatform</p>
 * <p>Title: ShuMiTradeServiceImpl.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */
public class ShuMiTradeServiceImpl implements ShuMiTradeService{
    private TradeDao tradeDao = new TradeDaoImpl();
    private CustomerService customerService = new CustomerService();
    private CapitalService capitalService = new CapitalServiceImpl();
    private OpenAccountPactService openAccountPactService = new OpenAccountPactServiceImpl();
    private AccountService accountService = new AccountServiceImpl();
    private ProductService productService = new ProductServiceImpl();
    private BankCardService bankCardService = new BankCardServiceImpl();
    private BankService bankService = new BankServiceImpl();

    @Override
    public void shuMiTradeOrder(ShuMiTradeFormVo shuMiTradeFormVo, String token) {
        CustomerSession customerSession = customerService.getCustomerSession(token);
        String customerId = customerSession.getCustomerId();

        //开户银行卡信息
        createOpenAccountBankInfo(shuMiTradeFormVo, customerId);

        FundNav fundNav = productService.findFundNavByCode(shuMiTradeFormVo.getFundCode());
        //子帐号
        accountService.createSubAccount(customerId, fundNav.getIaGuid(), DictConst.FP_PRODUCT_TYPE_1);

        //下单记录
        Trade trade = createTrade(shuMiTradeFormVo, customerId, DictConst.TRADE_TYPE_1);

    }

    @Override
    public void shuMiTradeRedeem(ShuMiTradeFormVo shuMiTradeFormVo, String token) {
        CustomerSession customerSession = customerService.getCustomerSession(token);
        String customerId = customerSession.getCustomerId();
        createTrade(shuMiTradeFormVo, customerId, DictConst.TRADE_TYPE_2);
    }

    private void createOpenAccountBankInfo(ShuMiTradeFormVo shuMiTradeFormVo, String customerId) {
        String bankName = shuMiTradeFormVo.getBankName();
        String bankCardNo = shuMiTradeFormVo.getBankAcco();

        BankCardVo bankCardVo = new BankCardVo();
        bankCardVo.setBankCardNo(bankCardNo);
        bankCardVo.setBankName(bankName);
//        Bank bank = bankService.findBankByBankName(bankName);
//        bankCardVo.setBankCode(bank.getBankCode());
        openAccountPactService.createFundOpenAccount(customerId, bankCardVo);
    }

    private Trade createTrade(ShuMiTradeFormVo shuMiTradeFormVo, String customerId, String type){
        String applySum = shuMiTradeFormVo.getApplySum();
        String fundCode = shuMiTradeFormVo.getFundCode();
        String fundName = shuMiTradeFormVo.getFundName();
        String bankName = shuMiTradeFormVo.getBankName();
        String bankCardNo = shuMiTradeFormVo.getBankAcco();
        String applySerial = shuMiTradeFormVo.getApplySerial();

        Timestamp currentTime = DBHelper.getCurrentTime();
        Date dateTime = null;
        try {
            if (shuMiTradeFormVo.getDateTime() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
                dateTime = sdf.parse(shuMiTradeFormVo.getDateTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Trade trade = new Trade();
        trade.setTradeNo(applySerial);
        trade.setType(type);
        trade.setTradeAmount(new BigDecimal(applySum));
        trade.setTradeStatus(DictConst.TRADE_STATUS_1);//途中
        trade.setConfirmStatus(DictConst.VERIFY_STATUS_1);//1-待确认
        trade.setTradeTime(dateTime == null ? currentTime : dateTime);
        trade.setCreateTime(currentTime);
        trade.setCustId(customerId);
        trade.setBankCardNo(bankCardNo);
        trade.setBankName(bankName);
        trade.setPayStatus(DictConst.PAYMENT_STATUS_2);//未付款
        trade.setProductCode(fundCode);
        trade.setProductName(fundName);
//        trade.setProductPrice(fundNav.getPurchaseLimitMin());
//        trade.setQuantity(Integer.valueOf(new BigDecimal(applySum).divide(fundNav.getPurchaseLimitMin()).toString()));
//        trade.setFee(BigDecimal.valueOf(fundNav.getCharge()));
        return tradeDao.saveTrade(trade);
    }

}
