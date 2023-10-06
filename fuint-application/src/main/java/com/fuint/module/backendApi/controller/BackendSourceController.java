package com.fuint.module.backendApi.controller;

import com.fuint.common.domain.TreeNode;
import com.fuint.common.domain.TreeSelect;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.SourceDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.SourceService;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.TSource;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-后台菜单相关接口")
@RestController
@RequestMapping(value = "/backendApi/source")
public class BackendSourceController extends BaseController {

    @Autowired
    private SourceService sSourceService;

    /**
     * 菜单服务接口
     * */
    @Autowired
    private SourceService sourceService;

    /**
     * 获取菜单列表
     *
     * @return 账户信息列表展现页面
     */
    @ApiOperation(value = "获取菜单列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseObject list(HttpServletRequest request) {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        List<TreeNode> sources = sSourceService.getSourceTree(accountInfo.getMerchantId());
        return getSuccessResult(sources);
    }

    /**
     * 获取菜单详情
     *
     * @param sourceId 菜单ID
     * @return 菜单信息
     */
    @ApiOperation(value = "获取菜单详情")
    @RequestMapping(value = "/info/{sourceId}", method = RequestMethod.GET)
    public ResponseObject info( @PathVariable("sourceId") Long sourceId) {
        TSource tSource = sSourceService.getById(sourceId);

        SourceDto sourceDto = new SourceDto();
        sourceDto.setId(tSource.getSourceId());
        sourceDto.setName(tSource.getSourceName());
        if (tSource.getParentId() != null) {
            sourceDto.setParentId(tSource.getParentId());
        }
        sourceDto.setMerchantId(tSource.getMerchantId());
        sourceDto.setPath(tSource.getPath());
        sourceDto.setIcon(tSource.getNewIcon());
        sourceDto.setNewIcon(tSource.getNewIcon());
        sourceDto.setSort(tSource.getSourceStyle());
        sourceDto.setEname(tSource.getEname());
        sourceDto.setStatus(tSource.getStatus());
        sourceDto.setIsMenu(tSource.getIsMenu());
        sourceDto.setPerms(tSource.getPath().replaceAll("/", ":"));
        sourceDto.setDescription(tSource.getDescription());

        return getSuccessResult(sourceDto);
    }

    /**
     * 新增菜单
     *
     * @return
     * @throws BusinessCheckException
     */
    @ApiOperation(value = "新增菜单")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseObject addSource(HttpServletRequest request, @RequestBody Map<String, Object> param) {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        String name = param.get("name").toString();
        String status = param.get("status").toString();
        String parentId = param.get("parentId").toString();
        String icon = param.get("icon").toString();
        String path = param.get("path").toString();
        String sort = param.get("sort").toString();
        Integer isMenu = param.get("isMenu") == null ? 1 : Integer.parseInt(param.get("isMenu").toString());

        TSource addSource = new TSource();
        addSource.setSourceName(name);
        addSource.setMerchantId(accountInfo.getMerchantId());
        addSource.setStatus(status);
        addSource.setNewIcon(icon);
        addSource.setIsLog(1);
        addSource.setPath(path);
        addSource.setSourceStyle(sort);
        addSource.setIsMenu(isMenu);
        addSource.setSourceCode(path);

        String eName = "";
        String[] paths = path.split("/");
        for (int i = 0; i < paths.length; i++) {
             eName = eName + CommonUtil.firstLetterToUpperCase(paths[i]);
        }
        addSource.setEname(eName);

        if (StringUtil.isNotBlank(parentId)) {
            if (Integer.parseInt(parentId) > 0) {
                TSource parentSource = sSourceService.getById(parentId);
                addSource.setParentId(parentSource.getSourceId());
                addSource.setSourceLevel(parentSource.getSourceLevel() + 1);
            } else {
                addSource.setSourceLevel(1);
            }
        } else {
            addSource.setSourceLevel(1);
        }
        try {
            sSourceService.addSource(addSource);
        } catch (Exception e) {
            return getFailureResult(201, e.getMessage());
        }
        return getSuccessResult(true);
    }

    /**
     * 修改菜单
     *
     * @param param
     * @return
     */
    @ApiOperation(value = "修改菜单")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseObject update(HttpServletRequest request, @RequestBody Map<String, Object> param) {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        String name = param.get("name").toString();
        String status = param.get("status").toString();
        String parentId = param.get("parentId").toString();
        String icon = param.get("icon").toString();
        String path = param.get("path").toString();
        String sort = param.get("sort").toString();
        Integer isMenu = param.get("isMenu") == null ? 1 : Integer.parseInt(param.get("isMenu").toString());
        Long id = param.get("id") == null ? 0 : Long.parseLong(param.get("id").toString());

        TSource editSource = sSourceService.getById(id);
        if (!editSource.getMerchantId().equals(accountInfo.getMerchantId()) && accountInfo.getMerchantId() > 0) {
            return getFailureResult(201, "抱歉，您没有修改的权限");
        }
        editSource.setSourceName(name);
        editSource.setStatus(status);
        editSource.setNewIcon(icon);
        editSource.setIsLog(1);
        editSource.setPath(path);
        editSource.setSourceStyle(sort);
        editSource.setIsMenu(isMenu);
        editSource.setSourceCode(editSource.getPath());

        String eName = "";
        String[] paths = path.split("/");
        for (int i = 0; i < paths.length; i++) {
             eName = eName + CommonUtil.firstLetterToUpperCase(paths[i]);
        }
        editSource.setEname(eName);

        if (StringUtil.isNotBlank(parentId)) {
            try {
                if (Integer.parseInt(parentId) > 0) {
                    TSource parentSource = sSourceService.getById(Long.parseLong(parentId));
                    editSource.setParentId(parentSource.getSourceId());
                    editSource.setSourceLevel(parentSource.getSourceLevel() + 1);
                } else {
                    editSource.setSourceLevel(1);
                }
            } catch (Exception e) {
                return getFailureResult(201, "父菜单" + parentId + "不存在");
            }
        } else {
            editSource.setSourceLevel(1);
        }
        try {
            sSourceService.editSource(editSource);
        } catch (Exception e) {
            return getFailureResult(201, e.getMessage());
        }
        return getSuccessResult(true);
    }

    /**
     * 删除菜单
     *
     * @return
     * @throws BusinessCheckException
     */
    @ApiOperation(value = "删除菜单")
    @RequestMapping(value = "/delete/{sourceId}", method = RequestMethod.GET)
    public ResponseObject delete(HttpServletRequest request, @PathVariable("sourceId") Long sourceId) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }
        try {
            TSource tSource = sSourceService.getById(sourceId);
            if (!tSource.getMerchantId().equals(accountInfo.getMerchantId()) && accountInfo.getMerchantId() > 0) {
                return getFailureResult(201, "抱歉，您没有删除的权限");
            }
            tSource.setStatus(StatusEnum.DISABLE.getKey());
            sSourceService.editSource(tSource);
        } catch(Exception e) {
            return getFailureResult(201,"存在子菜单或已关联角色,不能删除.");
        }

        return getSuccessResult(true);
    }

    /**
     * 获取菜单下拉树列表
     * */
    @ApiOperation(value = "获取菜单下拉树列表")
    @RequestMapping(value = "/treeselect", method = RequestMethod.GET)
    public ResponseObject treeselect(HttpServletRequest request) {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        List<TreeNode> sources = sSourceService.getSourceTree(accountInfo.getMerchantId());
        List<TreeSelect> data = sourceService.buildMenuTreeSelect(sources);

        return getSuccessResult(data);
    }
}
