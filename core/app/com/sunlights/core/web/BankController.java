package com.sunlights.core.web;

import com.sunlights.common.AppConst;
import com.sunlights.common.MsgCode;
import com.sunlights.common.Severity;
import com.sunlights.common.utils.MessageUtil;
import com.sunlights.common.vo.Message;
import com.sunlights.common.vo.PageVo;
import com.sunlights.core.service.BankCardService;
import com.sunlights.core.service.BankService;
import com.sunlights.core.service.impl.BankCardServiceImpl;
import com.sunlights.core.service.impl.BankServiceImpl;
import com.sunlights.core.vo.BankCardFormVo;
import com.sunlights.core.vo.BankCardVo;
import com.sunlights.core.vo.BankVo;
import com.sunlights.customer.service.impl.CustomerService;
import models.CustomerSession;
import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Map;

/**
 * <p>Project: fsp</p>
 * <p>Title: BankService.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:zhencai.yuan@sunlights.cc">yuanzhencai</a>
 */
@Transactional
public class BankController extends Controller {

    private Form<PageVo> pagerForm = Form.form(PageVo.class);
    private Form<BankCardFormVo> bankCardForm = Form.form(BankCardFormVo.class);

    private BankService bankService = new BankServiceImpl();
    private BankCardService bankCardService = new BankCardServiceImpl();
    private CustomerService customerService = new CustomerService();

    public Result createBankCard() {
        BankCardVo bankCardVo = null;
        Http.RequestBody body = request().body();
        if (body.asJson() != null) {
            bankCardVo = Json.fromJson(body.asJson(), BankCardFormVo.class);
        }
        if (body.asFormUrlEncoded() != null) {
            bankCardVo = Form.form(BankCardVo.class).bindFromRequest().get();
        }
        
        Map<String, String> params = Form.form().bindFromRequest().data();
        Logger.debug("=====createBankCard params=====" + Json.toJson(params));
        
        play.Logger.info("[bankCardVo]" + Json.toJson(bankCardVo));
        customerService.validateCustomerSession(request(), session(), response());
        String token = request().cookie(AppConst.TOKEN).value();
        bankCardService.createBankCard(token, bankCardVo);
        return ok(MessageUtil.getInstance().toJson());
    }


    public Result deleteBankCards() {
        BankCardFormVo bankCardVo = null;
        Http.RequestBody body = request().body();
        if (body.asJson() != null) {
            bankCardVo = Json.fromJson(body.asJson(), BankCardFormVo.class);
        }
        if (body.asFormUrlEncoded() != null) {
            bankCardVo = bankCardForm.bindFromRequest().get();
        }

        Map<String, String> params = Form.form().bindFromRequest().data();
        Logger.debug("=====createBankCard params=====" + Json.toJson(params));
        play.Logger.info("[bankCardVo]" + Json.toJson(bankCardVo));

        CustomerSession customerSession = customerService.validateCustomerSession(request(), session(), response());
        bankCardService.deleteBankCard(customerSession.getToken(), bankCardVo);
        return ok(MessageUtil.getInstance().toJson());
    }

    public Result validateBankCard() {
        BankCardFormVo bankCardVo = null;
        Http.RequestBody body = request().body();
        if (body.asJson() != null) {
            bankCardVo = Json.fromJson(body.asJson(), BankCardFormVo.class);
        }
        if (body.asFormUrlEncoded() != null) {
            bankCardVo = bankCardForm.bindFromRequest().get();
        }
        play.Logger.info("[bankCardVo]" + Json.toJson(bankCardVo));
        Http.Cookie cookie = Controller.request().cookie(AppConst.TOKEN);
        String token = cookie == null ? null : cookie.value();
        customerService.validateCustomerSession(request(), session(), response());
        boolean validated = bankService.validateBankCard(token, bankCardVo);
        if (validated) {
            MessageUtil.getInstance().setMessage(new Message(Severity.INFO, MsgCode.OPERATE_SUCCESS), true);
        } else {
            MessageUtil.getInstance().setMessage(new Message(Severity.INFO, MsgCode.BANK_CARD_CERTIFY_FAIL), false);
        }
        return ok(MessageUtil.getInstance().toJson());
    }

    public Result findBankCards() {
        PageVo pageVo = new PageVo();
        Http.RequestBody body = request().body();
        if (body.asJson() != null) {
            pageVo = Json.fromJson(body.asJson(), PageVo.class);
        }
        if (body.asFormUrlEncoded() != null) {
            pageVo = pagerForm.bindFromRequest().get();
        }
        Http.Cookie cookie = Controller.request().cookie(AppConst.TOKEN);
        String token = cookie == null ? null : cookie.value();
        customerService.validateCustomerSession(request(), session(), response());
        bankCardService.findBankCardsByToken(token, pageVo);
        return ok(MessageUtil.getInstance().toJson());
    }

    public Result findBankByBankCardNo() {
        BankCardFormVo bankCardVo = null;
        Http.RequestBody body = request().body();
        if (body.asJson() != null) {
            bankCardVo = Json.fromJson(body.asJson(), BankCardFormVo.class);
        }
        if (body.asFormUrlEncoded() != null) {
            bankCardVo = bankCardForm.bindFromRequest().get();
        }
        play.Logger.info("[bankCardVo]" + Json.toJson(bankCardVo));
        BankVo bankVo = bankService.findBankByBankCardNo(bankCardVo.getBankCardNo());
        MessageUtil.getInstance().setMessage(new Message(Severity.INFO, MsgCode.OPERATE_SUCCESS), bankVo);
        return ok(MessageUtil.getInstance().toJson());
    }

    public Result findBanks() {
        PageVo pageVo = new PageVo();
        Http.RequestBody body = request().body();

        if (body.asJson() != null) {
            pageVo = Json.fromJson(body.asJson(), PageVo.class);
        }
        if (body.asFormUrlEncoded() != null) {
            pageVo = pagerForm.bindFromRequest().get();
        }
        play.Logger.info("[pager]" + Json.toJson(pageVo));
        List<BankVo> bankVos = bankService.findBanksBy(pageVo);
        MessageUtil.getInstance().setMessage(new Message(Severity.INFO, MsgCode.OPERATE_SUCCESS), bankVos);
        return ok(MessageUtil.getInstance().toJson());
    }
}
