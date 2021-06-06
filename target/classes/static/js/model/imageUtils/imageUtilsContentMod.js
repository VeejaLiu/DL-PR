define(['api', 'utils'], function(api, utils){

    // 树结构对象
    var treeId = "#treeId";
    var plateTreeNode = null;
    var dirTreeNode = null;

    // canvas绘图对象
    var canvas = document.getElementById("canvas");
    var ctxt = canvas.getContext('2d');

    var imgDest = document.getElementById("imgDest");
    var ctxtDest = imgDest.getContext('2d');

    var img = null;
    var imgData = null;
    var hsvRange = {};
    hsvRange['minH'] = 0;
    hsvRange['maxH'] = 180;
    hsvRange['minS'] = 0;
    hsvRange['maxS'] = 255;
    hsvRange['minV'] = 0;
    hsvRange['maxV'] = 255;

    var tempImgPath = "";

    // 多边形提取，顶点
    var points = [/*{x:50, y:0}, {x:50, y:50}, {x:0, y:50}*/];
    var clip = false;

    function init(){
        initTree();
        bindBtnEvent();
        initSilder();
    }

    function bindBtnEvent(){
        $("#canvas").on('click', function (evt) {
            var p = this;
            var _x = 0, _y = 0;
            while(p.offsetParent){
                _x += p.offsetLeft;
                _y += p.offsetTop;
                p = p.offsetParent;
            }
            _x += p.offsetLeft; // 图片起点坐标
            _y += p.offsetTop;  // 图片起点坐标

            $("#c_clos").val($(this).width());
            $("#c_rows").val($(this).height());
            var x = evt.clientX - _x;
            var y = evt.clientY - _y;
            $("#p_clos").val(x); // 鼠标点击位置相对起点坐标
            $("#p_rows").val(y); // 鼠标点击位置相对起点坐标

            x = Math.round($("#clos").val() / $(this).width() * x); // 计算原图大小对应鼠标点击位置的坐标
            y = Math.round($("#rows").val() / $(this).height() * y);

            if(clip){   // 提取多边形
                ctxt.lineWidth = 2;
                ctxt.strokeStyle = "#ff0000";
                setPoints(x, y);
            }

            // 获取坐标点rgb颜色data
            var data = ctxt.getImageData(x, y, 1, 1).data;
            $("#rgbValue").val(data.slice(0,3).join(", ")); // 显示rgb值
            $("#rgbColor").css("background-color", "rgba("+ $("#rgbValue").val() + ")"); // 显示rgb颜色
            $("#hsvValue0").val(rgbToHsv(data.slice(0,3)).join(", ")); // 显示rgb转hsv后的值

            // 发起后端请求，获取指定坐标下的hsv值；用于对比前后端算法的结果是否一致
            function successFun(ret) {
                if (ret.code === 200) {
                    $("#hsvValue1").val(ret.obj['HSV']);
                } else {
                    layer.msg(ret.msg, {icon: 2});
                }
            }
            var option = {
                type: 'post',
                url: api.plate.getHSVValue,
                success: successFun,
                data: {"imgPath": tempImgPath, "row": y, "col": x} // 需要注意xy坐标跟row col的对应关系
            };
            console.log(tempImgPath);
            utils.ajax(option);
        });

        $("#clip").on("click", function () {
            clip = true;
            // 清理掉原图的绘画痕迹 -- 未完成

        });
    }

    function clipPolygon() {
        imgDest.width = img.width;
        imgDest.height = img.height;
        ctxtDest.save();
        ctxtDest.beginPath();
        ctxtDest.moveTo(points[0].x, points[0].y);
        $.each(points, function (index, item) {
            if(index != 0){
                ctxtDest.lineTo(points[index].x, points[index].y);
            }
        });
        ctxtDest.closePath();
        ctxtDest.clip();
        ctxtDest.stroke();
        ctxtDest.drawImage(img,0, 0, img.width, img.height);
        ctxtDest.restore();
        ctxtDest.draw();
    }

    function setPoints(x, y){
        points.push({'x':x, 'y':y});
        if(points.length == 1){
            ctxt.beginPath();
            ctxt.moveTo(x, y);
        } else {
            ctxt.lineTo(x, y);
        }
        if (points.length >= 4) {
            ctxt.closePath();   // 闭合区间
            clip = false;
        }
        ctxt.stroke(); //画线轮廓
        if(!clip){
            clipPolygon();
        }
    }

    /**
     * 通过css样式，展示剪切后的多边形
     * 但是无法直接将剪切后的图保存下来
     */
    function clipPolygon1() {
        var polygon = "-webkit-clip-path: polygon(";
        $.each(points, function (index, item) {
            polygon = polygon + item.x + "px " + item.y + "px,";
        });
        polygon = polygon.substr(0, polygon.length-1) + ")";
        $("#imgDest").attr("src","./test.jpg");
        $("#imgDest").attr("style", polygon);
    }

    // hsv 色彩范围过滤   // 色彩分割
    function hsvColorFilter(){
        var d = ctxt.createImageData(img.width, img.height);// 创建 width*height像素的 ImageData 对象
        for(var i =0; i<imgData.length; i+=4){
            var p = [];
            p[0] = imgData[i]; //red红色色深
            p[1] = imgData[i + 1]; //green绿色色深
            p[2] = imgData[i + 2];  //blue蓝色色深
            // p[3] = imgData[i + 3];  //alpha透明度
            var hsv = rgbToHsv(p); //  转换成hsv的值，统一用一个算法，保证结果一致性
            if(checkInRange(hsv)){// 颜色范围内，保留原有颜色
                d.data[i] = imgData[i];
                d.data[i+1] = imgData[i+1];
                d.data[i+2] = imgData[i+2];
                d.data[i+3] = imgData[i+3];
            } else {    // 颜色范围外，替换成白色
                d.data[i] = 255;
                d.data[i+1] = 255;
                d.data[i+2] = 255;
                d.data[i+3] = 255;
            }
        }
        ctxt.putImageData(d, 0, 0);  // 重新绘制图片
    }

    function checkInRange(hsv){
        var bl = true;
        if(hsvRange['minH'] > hsv[0] || hsv[0] > hsvRange['maxH'] ){
            bl = false;
        }
        if(hsvRange['minS'] > hsv[1] || hsv[1] > hsvRange['maxS'] ){
            bl = false;
        }
        if(hsvRange['minV'] > hsv[2] || hsv[2] > hsvRange['maxV'] ){
            bl = false;
        }
        return bl;
    }

    function initTree() {
        isFirst = false; //加载树的时候默认咱开第一层级
        $.fn.zTree.destroy(treeId);
        $.fn.zTree.init($(treeId), setting);
    }

    function initSilder(data) {
        $('.hRange').jRange({
            from: 0,
            to: 180,
            step: 1,
            scale: [0,60,120,180],
            format: '%s',
            width: "90%",
            showLabels: true,
            isRange : true,
            ondragend: function () {
                hsvRange['minH'] = Number(this.getValue().split(',')[0]);
                hsvRange['maxH'] = Number(this.getValue().split(',')[1]);
                hsvColorFilter();
            }
        });

        $('.sRange').jRange({
            from: 0,
            to: 255,
            step: 1,
            scale: [0,50,100,150,200,250],
            format: '%s',
            width: "90%",
            showLabels: true,
            isRange : true,
            theme: 'theme-blue',
            ondragend: function () {
                hsvRange['minS'] = Number(this.getValue().split(',')[0]);
                hsvRange['maxS'] = Number(this.getValue().split(',')[1]);
                hsvColorFilter();
            }
        });


        $('.vRange').jRange({
            from: 0,
            to: 255,
            step: 1,
            scale: [0,50,100,150,200,250],
            format: '%s',
            width: "90%",
            showLabels: true,
            isRange : true,
            ondragend: function () {
                hsvRange['minV'] = Number(this.getValue().split(',')[0]);
                hsvRange['maxV'] = Number(this.getValue().split(',')[1]);
                hsvColorFilter();
            }
        });
    }

    // 树结构配置
    var setting = {
        edit: {
            enable: true,
            editNameSelectAll: true,
            showRemoveBtn: true,
            showRenameBtn: true
        },
        view: {
            addHoverDom: addHoverDom,
            removeHoverDom: removeHoverDom
        },
        check: {
            enable: false
        },
        callback: {
            onClick: treeClick,
            onAsyncSuccess:onAsyncSuccess,
            beforeRemove: beforeRemove,
            beforeRename: beforeRename,
        },
        async: {
            enable: true,
            url: api.file.getFileTreeByDir,
            type: 'get',
            dataType: "json",
            autoParam: ["id=dir"],
            otherParam: {"typeFilter":"png,jpg,jpeg", "rootPath": "D:/PlateDetect/"},
            dataFilter: ajaxDataFilter
        },
        data: {
            simpleData: {
                enable: true
            }
        }
    };

    // 添加刷新按钮
    function addHoverDom(treeId, treeNode) {
        var aObj = $("#" + treeNode.tId + "_a");
        if(!treeNode.isParent){
            return;
        }
        if ($("#" + treeNode.tId + "_refresh").length > 0){
            return;
        }
        var refreshStr = $('<button type="button" class="icon-refresh" id="'+treeNode.tId+'_refresh" title="refresh" treenode_refresh=""></button >');
        aObj.append(refreshStr);
        refreshStr.bind("click", function(){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.reAsyncChildNodes(treeNode, "refresh");
        });
    };
    // 移除刷新按钮
    function removeHoverDom(treeId, treeNode) {
        $("#" + treeNode.tId + "_refresh").unbind().remove();
    };

    function beforeRemove(treeId, treeNode) {
        layer.confirm("是否删除？", function(index){
            function successFun(ret) {
                if (ret.code === 200) {
                    layer.msg("删除成功", {icon: 1});
                    var treeObj = $.fn.zTree.getZTreeObj(treeId);
                    treeObj.reAsyncChildNodes(treeNode.getParentNode(), "refresh");
                } else {
                    layer.msg(ret.msg, {icon: 2});
                }
            }
            var option = {
                type: 'get',
                url: api.plate.removeDirOrFile,
                success: successFun,
                data: {"fileName": treeNode.filePath}
            };
            utils.ajax(option);
            layer.close(index);
        });
        return false;
    }


    function beforeRename(treeId, treeNode, newName, isCancel) {
        function successFun(ret) {
            if (ret.code === 200) {
                var treeObj = $.fn.zTree.getZTreeObj(treeId);
                treeObj.reAsyncChildNodes(treeNode.getParentNode(), "refresh");
            } else {
                layer.msg(ret.msg, {icon: 2});
            }
        }
        var option = {
            type: 'get',
            url: api.plate.renameDirOrFile,
            success: successFun,
            data: {"fileName": treeNode.filePath, "newName": newName}
        };
        utils.ajax(option);

        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        treeObj.refresh(treeNode);
        return false;
    }


    var isFirst = false;
    function onAsyncSuccess(event, treeId) {
        if (isFirst) {
            //获得树形图对象
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var nodes = treeObj.getNodes();
            if (nodes.length>0) {
                for(var i=0;i<nodes.length;i++){
                    if(nodes[i].isParent){
                        treeObj.expandNode(nodes[i], true, false, false); // 展开第一层级
                    }
                }
            }
            isFirst= false;
        }
    }

    // 异步加载树结构数据
    function ajaxDataFilter(treeId, parentNode, ret) {
        var treeNode = [];
        if (ret.code === 200) {
            $.each(ret.obj, function (index, item){
                var node = {};
                node.id = item.id;
                node.name = item.fileName;
                node.isParent = item.isDir;
                node.filePath = encodeURI(item.filePath);   // 路径编码，防止出现特殊字符影响
                treeNode.push(node);
            });
        }
        return treeNode;
    };

    // 树节点点击事件
    function treeClick(event, treeId, node) {
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        img = new Image();
        img.src = encodeURI(api.file.readFile + "?filePath=" + node.filePath);
        // 发起后端请求，获取opencv解读后的图片信息
        function successFun(ret) {
            if (ret.code === 200) {
                tempImgPath = ret.obj.targetPath;
            } else {
                layer.msg(ret.msg, {icon: 2});
            }
        }

        var option = {
            type: 'post',
            url: api.plate.getImgInfo,
            success: successFun,
            data: {"imgPath": node.filePath}
        };
        utils.ajax(option);

        setTimeout(function () {
            $("#clos").val(img.width);
            $("#rows").val(img.height);
            canvas.width = img.width;
            canvas.height = img.height;
            ctxt.drawImage(img,0, 0, img.width, img.height);
            imgData = ctxt.getImageData(0, 0, img.width, img.height).data;
            hsvColorFilter();
            $('.hRange').jRange('setValue', hsvRange['minH'] + "," + hsvRange['maxH']);
            $('.sRange').jRange('setValue', hsvRange['minS'] + "," + hsvRange['maxS']);
            $('.vRange').jRange('setValue', hsvRange['minV'] + "," + hsvRange['maxV']);
        }, 500);

        if(node.isParent){
            $("#parentDir").val(node.filePath);
            dirTreeNode = node;
        } else {
            plateTreeNode = node;
        }
    }


    //参数arr的值分别为[r,g,b]
    function rgbToHsv(arr) {
        var h = 0, s = 0, v = 0;
        var r = arr[0], g = arr[1], b = arr[2];
        arr.sort(function (a, b) {
            return a - b;
        });
        var max = arr[2]
        var min = arr[0];
        if (max === 0) {
            s = 0;
        } else {
            s = 1 - (min / max);
        }
        if (max === min) {
            h = 0;//事实上，max===min的时候，h无论为多少都无所谓
        } else if (max === r && g >= b) {
            h = 60 * ((g - b) / (max - min)) + 0;
        } else if (max === r && g < b) {
            h = 60 * ((g - b) / (max - min)) + 360
        } else if (max === g) {
            h = 60 * ((b - r) / (max - min)) + 120
        } else if (max === b) {
            h = 60 * ((r - g) / (max - min)) + 240
        }
        h = Math.round(h/2);
        s = Math.round(s * 255);  // 转换到opencv的 0-255取值范围
        v = Math.round(max);
        return [h, s, v]
    }


    //参数arr的3个值分别对应[h, s, v]
    function hsvToRgb(arr) {
        var h = arr[0], s = arr[1], v = arr[2];
        s = s / 100;
        v = v / 100;
        var r = 0, g = 0, b = 0;
        var i = Math.round((h / 60) % 6);
        var f = h / 60 - i;
        var p = v * (1 - s);
        var q = v * (1 - f * s);
        var t = v * (1 - (1 - f) * s);
        switch (i) {
            case 0:
                r = v; g = t; b = p;
                break;
            case 1:
                r = q; g = v; b = p;
                break;
            case 2:
                r = p; g = v; b = t;
                break;
            case 3:
                r = p; g = q; b = v;
                break;
            case 4:
                r = t; g = p; b = v;
                break;
            case 5:
                r = v; g = p; b = q;
                break;
            default:
                break;
        }
        r = Math.round(r * 255.0)
        g = Math.round(g * 255.0)
        b = Math.round(b * 255.0)
        return [r, g, b];
    }

    return {
        "init": init
    }
});