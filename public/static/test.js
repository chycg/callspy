$(function () {

	var lastResult;
	var socketUrl = "ws://localhost:9000/socket";
	var testAllResult;
	var socket;

	var s = {
		currentMode: '',

		showDialog: function (mode) {
			s.currentMode = mode;
			v.dialog = true;
		},

		checkResult: function (result, success) {
			var preId = "#result";
			if (v.tabValue == 'tab5') {
				preId = "#docs";
			}

			if (!result.success) {
				if (result.exception == null) {
					$(preId).JSONView(result);
				} else {
					var msg = result.exception;
					if (result.connectionError == true) {
						msg = 'http连接异常，请确认：1.服务器地址和端口配置项；2.服务器状态是否正常\n' + msg;
					}
					$(preId).text(msg).css("outline-color", "#f00");
				}
			} else if (success != null) {
				success(result);
				$(preId).css("outline-color", "");
			}
		},

		doGet: function () {
			var url = arguments[0];
			var data = null;
			var success = null;

			if (arguments.length == 2) {
				var v = arguments[1];
				if (typeof v == "function") {
					success = v;
				} else {
					data = v;
				}
			} else if (arguments.length == 3) {
				data = arguments[1];
				success = arguments[2];
			}

			$.get(url, data, function (result) {
				s.checkResult(result, success);
			});
		},

		doPost: function () {
			var url = arguments[0];
			var data, success;

			if (arguments.length == 2) {
				var v = arguments[1];
				if (typeof v == "function") {
					success = v;
				} else {
					data = v;
				}
			} else if (arguments.length == 3) {
				data = arguments[1];
				success = arguments[2];
			}

			$.post(url, data, function (result) {
				s.checkResult(result, success);
			});
		},

		postJson: function () {
			var url = arguments[0];
			var data, success;

			if (arguments.length == 2) {
				var v = arguments[1];
				if (typeof v == "function") {
					success = v;
				} else {
					data = v;
				}
			} else if (arguments.length == 3) {
				data = arguments[1];
				success = arguments[2];
			}

			$.ajax({
				type: "post",
				url: url,
				data: JSON.stringify(data),
				async: true, // 使用同步方式
				contentType: "application/json; charset=utf-8",
				dataType: "json",
				success: function (result) {
					s.checkResult(result, success);
				}
			});
		},

		isEmpty(str) {
			return str == null || str.trim().length == 0;
		}
	};

	var v = new Vue({
		el: '#test',
		data: {
			dialog: false,
			config: false,
			effect: false,
			matchMode: 1,

			open: [],

			classInfo: [], // all
			showedItems: [], // showing

			classInfo2: [], // often all
			showedItems2: [], // often showing

			selectItem: "", // selected sidebar menu

			methodType: "-",
			methodPath: "",
			methodInfo: "",
			methodComment: "",

			path: null,
			pathList: [],
			class0: null,
			method0: null,

			testLog: "",
			rpcUrl: "",

			oftenSize: 3,
			filter: null,

			tabValue: "tab4",
			configTab: "tab1",

			token: "", // yapi token
			userToken: "", // 登录账号token

			menus: [], // 上级menu
			submenus: [], // 下级menu

			catId: "",  // 当前分类id
			apiId: "",

			inputValue: "",
			newDesc: "",
			preMode: false,

			tableColumns: [{
				title: 'Name',
				slot: 'name'
			},
			{
				title: 'Age',
				key: 'age'
			},
			{
				title: 'Address',
				key: 'address'
			},
			{
				title: 'Action',
				slot: 'action',
				width: 150,
				align: 'center'
			}],
			tableData: [{
				name: 'John Brown',
				age: 18,
				address: 'New York No. 1 Lake Park'
			},
			{
				name: 'Jim Green',
				age: 24,
				address: 'London No. 1 Lake Park'
			},
			{
				name: 'Joe Black',
				age: 30,
				address: 'Sydney No. 1 Lake Park'
			},
			{
				name: 'Jon Snow',
				age: 26,
				address: 'Ottawa No. 2 Lake Park'
			}]
		},

		methods: {
			hasNoMethod: function () {
				return v == null || s.isEmpty(v.class0) || s.isEmpty(v.method0);
			},

			showTip: function (info) {
				this.$Notice.warning({
					title: "Tip",
					desc: info,
					duration: 2
				});
			},

			showSuccess: function (info) {
				this.$Notice.success({
					title: "success",
					desc: info,
					duration: 2
				});
			},

			onSelect: function (name) {
				v.selectItem = name;
				v.config = name == "configs";

				if (v.config) {
					s.doGet("getConfigs.to", function (result) {
						var data = result.data;
						v.testLog = data.testLog;
						v.rpcUrl = data.rpcUrl;
						v.oftenSize = data.oftenSize;
						v.effect = data.effect;
						v.matchMode = data.matchMode;
						v.iface = data.iface;
						v.ifaceList = data.ifaceList;
						v.path = data.path;
						v.pathList = data.pathList;
					})
					return;
				}

				this.onTab();
			},

			onTab: function () {
				if (v.tabValue == "tab4") {
					this.loadMethod();
				} else if (v.tabValue == "tab5") {
					this.loadDocs();
				}
			},

			onTab2: function () {
				if (v.configTab != "tabTestAll")
					return;

				if (socket == null || socket.readyState != 1) {
					testAllResult = $("#testAllResult");
					socket = new WebSocket(socketUrl);
					socket.onopen = function () {
						console.log("--- socket connected!!")
					}

					socket.onmessage = function (msg) {
						testAllResult.append(msg.data + "\n");
						testAllResult[0].scrollTop = testAllResult[0].scrollHeight;
					}

					socket.onclose = function () {
						testAllResult.append("--- socket disconnected!!");
					}

					socket.onerror = function () {
						testAllResult.append("--- socket error!!");
					}
				}
			},

			loadMethod: function () { // load method
				var name = v.selectItem;

				var index = name.indexOf(".");
				v.class0 = name.substring(1, index);
				v.method0 = name.substring(index + 1, name.length);
				v.methodInfo = name.substring(1);

				if (name.trim().length == 0)
					return;

				$('#args').text("");
				s.doGet("getMethodArgs.to", {
					className: v.class0,
					methodName: v.method0
				}, function (result) {
					var data = result.data;
					$('#args').JSONView(data);

					if (result.attached != null) {
						if (result.attached.doc != null) {
							v.token = data.token;
							$("#docs").JSONView(result.attached.doc);
						}

						v.methodComment = result.attached.comment;
						if (result.attached.urlInfo != null) {
							v.methodType = result.attached.method;
							v.methodPath = result.attached.urlInfo
						}
					}
				});
			},

			loadDocs: function () { // load doc
				var name = v.selectItem;
				var index = name.indexOf(".");
				v.class0 = name.substring(1, index);
				v.method0 = name.substring(index + 1, name.length);
				v.methodInfo = name.substring(1);

				s.doGet("getMethodDocs.to", {
					token: v.token,
					className: v.class0,
					methodName: v.method0
				}, function (result) {
					var data = result.data;
					if (data != null) {
						$('#docs').JSONView(data);
					}

					if (result.attached != null) {
						v.token = result.attached.token;

						if (result.attached.urlInfo != null) {
							v.methodType = result.attached.method;
							v.methodPath = result.attached.urlInfo
						}

						v.methodComment = result.attached == null ? "" : result.attached.comment;
						v.menus = result.attached.menus;

						if (v.menus.length > 0) {
							if (v.catId != null) {
								var sameGroup = false;
								for (var i = 0; i < v.menus.length; i++) {
									if (v.menus[i].id == v.catId) {
										sameGroup = true;
										break;
									}
								}
							}

							if (!sameGroup)
								v.catId = v.menus[0].id;

							if (result.attached.cateId != null) {
								v.catId = result.attached.cateId;
								v.apiId = result.attached.apiId;

								v.onLoadApis(v.apiId);
							}
						}
					}
				});
			},

			onLoadApis: function (id) {
				s.doGet("getApis.to", {
					catId: v.catId,
					className: v.class0,
					methodName: v.method0
				}, function (result) {
					if (result.success) {
						var data = result.data;
						v.submenus = data;

						if (result.attached != null && result.attached.apiId != null)
							v.apiId = result.attached.apiId;
						else if (data.length > 0)
							v.apiId = data[0].id;
						if (id > 0)
							v.apiId = id;
						else
							v.apiId = "";
					}
				})
			},

			onLoadIface: function (path) {
				if (path == null)
					path = v.path;

				if (path == null)
					return;

				v.filter = null;
				v.classInfo2 = [];
				v.showedItems2 = [];

				s.doPost("loadIface.to", {
					path: path
				}, function (result) {
					var data = result.data;
					v.ifaceList = data.ifaceList;
					v.getAll();
					v.onFilter();
					v.pathList = data.pathList;
					v.path = data.path;

					if (data.host != null)
						v.rpcUrl = data.host;
				});
			},

			onAddPath: function () {
				v.inputValue = '';
				s.showDialog("onPath");
			},

			// 接口测试
			onTest: function () {
				if (this.hasNoMethod()) {
					v.showTip("请先选择接口与方法!!");
					return;
				}

				$("#result").text("");

				s.doPost("doTest.to", {
					className: v.class0,
					methodName: v.method0,
					args: $("#args").text()
				}, function (result) {
					lastResult = result;

					$("#result").JSONView(lastResult);
					var attached = result.attached;

					v.getOften();
				});
			},

			// 全局测试
			onTestOk: function () {
				if (typeof (WebSocket) == "undefined")
					return;

				s.doPost("autoTest.to", { type: 1 });
			},

			onTestError: function () {
				if (typeof (WebSocket) == "undefined")
					return;

				s.doPost("autoTest.to", { type: 0 });
			},

			onClearLog: function () {
				testAllResult.empty();
			},

			onClear: function () {
				if (this.hasNoMethod()) {
					v.showTip("请先选择接口与方法!!");
					return;
				}

				$("#args").text("");

				s.doGet("doClear.to", {
					className: v.class0,
					methodName: v.method0
				}, function (result) {
					var data = result.data;
					$('#args').JSONView(data);
				});
			},

			onEffect: function () {
				s.doPost("updateEffect.to", {
					effect: v.effect,
					matchMode: v.matchMode
				});
			},

			onPreMode: function () {
				s.doPost("updatePreMode.to", {
					preMode: v.preMode
				}, function (result) {
					v.rpcUrl = result.data;
				});
			},

			onRpcUrl: function () {
				s.doPost("updateRpcUrl.to", {
					url: v.rpcUrl
				});
			},

			onUserToken: function () {
				s.doPost("updateUserToken", {
					"token": v.userToken
				});
			},

			onToken: function () {
				if (v.token == null || v.token.trim() == "")
					return;

				this.loadDocs();
			},

			onOftenSize: function () {
				s.doPost("updateOftenSize.to", {
					oftenSize: v.oftenSize
				}, function () {
					v.getOften();
				});
			},

			getAll: function () {
				s.doGet("getAll.to", function (result) {
					if (result.success) {
						var data = result.data;
						v.classInfo = data;
						v.showedItems = data;
						v.onFilter();

						if (result.attached != null && result.attached.effect != null) {
							v.effect = result.attached.effect;
						}
					}
				});
			},

			okInput: function () {
				if (s.currentMode == "onCate") {
					s.doPost("addCate", {
						name: v.inputValue,
					}, function (result) {
						if (result.success) {
							var m = result.data;
							v.menus.push({ id: m.id, name: m.name });
							v.catId = m.id;
							v.showSuccess(result.msg);
						}
					});
				} else if (s.currentMode == "onApi") {
					if (this.hasNoMethod()) {
						v.showTip("请先选择接口与方法!!");
						return;
					}

					s.postJson("addApi", {
						name: v.inputValue,
						catId: v.catId,
						className: v.class0,
						methodName: v.method0,
						doc: $("#docs").text()
					}, function (result) {
						if (result.success) {
							var m = result.data;
							v.submenus.push({ id: m.id, name: m.name, path: m.path })
							v.apiId = m.id;
							v.showSuccess(result.msg);
						}
					});
				} else if (s.currentMode == "onPath") {
					v.onLoadIface(v.inputValue);
				}
			},

			cancelInput: function () {
				v.inputValue = '';
			},

			onCate: function () {
				v.inputValue = '';
				s.showDialog("onCate");
			},

			onApi: function () {
				if (this.hasNoMethod()) {
					v.showTip("请先选择接口与方法!!");
					return;
				}

				v.inputValue = v.methodComment;
				s.showDialog("onApi");
			},

			getOften: function () {
				s.doGet("getOften.to", function (result) {
					var data = result.data;
					if (data != null) {
						v.classInfo2 = data;
						v.onFilter();
					}
				});
			},

			removeOften: function (e) {
				s.doGet("removeOften.to", {
					className: $(e.target).prev().text().trim()
				}, function () {
					v.getOften();
				});
			},

			removePath: function (e) {
				var selection = $(e.target).prev().text().trim();
				if (v.path == selection) {
					v.showTip("当前路径不能被移除!!");
					return;
				}

				s.doPost("removePath", {
					path: selection
				}, function () {
					for (var i = 0; i < v.pathList.length; i++) {
						if (v.pathList[i] == selection) {
							v.pathList.splice(i, 1);
							break;
						}
					}
				});
			},

			changeArgs: function (e) {
				if (e.ctrlKey == true && e.button == 0) {
					var li = $(e.target).parent("li");
					li.remove();
				}
			},

			onFilter: function (arg) {
				var filter = v.filter;
				var array = JSON.parse(JSON.stringify(v.classInfo));
				var array2 = JSON.parse(JSON.stringify(v.classInfo2));

				if (filter != null && filter.trim().length > 0) {
					filter = filter.trim().toLowerCase();

					var endMatch = false;
					if (filter.endsWith("/")) {
						filter = filter.substring(0, filter.length - 1);
						endMatch = true;
					}

					for (var i = array.length - 1; i >= 0; i--) {
						var item = array[i];

						if (endMatch && item.name.toLowerCase().endsWith(filter))
							continue;

						if (!endMatch && item.name.toLowerCase().indexOf(filter) >= 0)
							continue;

						for (var k = item.methods.length - 1; k >= 0; k--) {
							var method = item.methods[k];
							if (method.toLowerCase().indexOf(filter) < 0) {
								item.methods.splice(k, 1);
							}
						}

						if (item.methods.length == 0)
							array.splice(i, 1);
					}

					for (var i = array2.length - 1; i >= 0; i--) {
						var item = array2[i];
						for (var k = item.methods.length - 1; k >= 0; k--) {
							var method = item.methods[k];
							if (method.toLowerCase().indexOf(filter) < 0)
								item.methods.splice(k, 1);
						}

						if (item.methods.length == 0)
							array2.splice(i, 1);
					}
				}

				v.showedItems = array;
				v.showedItems2 = array2;

				if (arg != null) { // 仅在筛选时做更新展开操作
					this.open = [];
					this.$nextTick(function () {
						this.$refs.leftMenu.updateOpened();
						this.$refs.leftMenu.updateActiveName();
					});

					if (v.showedItems.length <= 5) {
						for (var i = 0; i < v.showedItems.length; i++) {
							this.open.push('1' + v.showedItems[i].name);
						}

						// this.active = ["2-2"];
						this.$nextTick(function () {
							this.$refs.leftMenu.updateOpened();
							this.$refs.leftMenu.updateActiveName();
						});
					}
				}
			},
		}
	});

	v.getAll();
	v.getOften();
});