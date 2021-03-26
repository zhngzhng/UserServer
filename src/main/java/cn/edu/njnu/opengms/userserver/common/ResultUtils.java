package cn.edu.njnu.opengms.userserver.common;

/**
 * @ClassName ResultUtils
 * @Description 请求响应状态
 * @Author zhngzhng
 * @Date 2021/3/24
 **/
public class ResultUtils {
    public static JsonResult success(Object obj){
        JsonResult jsonResult = new JsonResult();
        jsonResult.setMsg(ResultEnum.SUCCESS.getMsg());
        jsonResult.setCode(ResultEnum.SUCCESS.getCode());
        jsonResult.setData(obj);
        return jsonResult;
    }

    public static JsonResult success(){
        return success(null);
    }


    public static JsonResult success(String msg){
        JsonResult jsonResult = new JsonResult();
        jsonResult.setMsg(msg);
        jsonResult.setCode(ResultEnum.SUCCESS.getCode());
        jsonResult.setData(null);
        return jsonResult;
    }

    public static JsonResult error(int code, String msg){
        JsonResult jsonResult = new JsonResult();
        jsonResult.setData(null);
        jsonResult.setCode(code);
        jsonResult.setMsg(msg);
        return jsonResult;
    }

    public static JsonResult error(String msg){
        return error(ResultEnum.ERROR.getCode(), msg);
    }

    public static JsonResult error(){
        return error(ResultEnum.ERROR.getCode(), ResultEnum.ERROR.getMsg());
    }


    public static JsonResult objExists(){
        return objExists(ResultEnum.EXISTS_OBJECT.getMsg());
    }

    public static JsonResult objExists(String msg){
        JsonResult jsonResult = new JsonResult();
        jsonResult.setMsg(msg);
        jsonResult.setCode(ResultEnum.EXISTS_OBJECT.getCode());
        jsonResult.setData(null);
        return jsonResult;
    }


    public static JsonResult noObject(){
        return noObject(ResultEnum.NO_OBJECT.getMsg());
    }

    public static JsonResult noObject(String msg){
        JsonResult jsonResult = new JsonResult();
        jsonResult.setData(null);
        jsonResult.setCode(ResultEnum.NO_OBJECT.getCode());
        jsonResult.setMsg(msg);
        return jsonResult;
    }

}
