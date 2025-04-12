<template>
	<view></view>
</template>

<script lang="uts">
	import SVG from 'com.caverock.androidsvg.SVG';
	import ImageView from 'android.widget.ImageView';
	import PictureDrawable from 'android.graphics.drawable.PictureDrawable';
	
	import { ButtonClickListener, createColoredBitmap, setSvgImage } from './utils'
	//原生提供以下属性或方法的实现
	export default {
		/**
		 * 组件名称，也就是开发者使用的标签
		 */
		name: "l-svg-x",
		/**
		 * 组件涉及的事件声明，只有声明过的事件，才能被正常发送
		 */
		emits: ['load', 'error', 'click'],
		/**
		 * 属性声明，组件的使用者会传递这些属性值到组件
		 */
		props: {
			"src": {
				type: String,
				default: ""
			},
			"color": {
				type: String,
				default: ""
			},
		},
		/**
		 * 组件内部变量声明
		 */
		data() {
			return {
				svg: null as SVG | null,
				timer: -1
			}
		},
		/**
		 * 属性变化监听器实现
		 */
		watch: {
			"src": {
				handler(_ : string) {
					if (this.src == '') return
					setTimeout(() => {
						this.update(true)
					}, 0)

				},
				immediate: true
			},
			"color": {
				handler(_ : string) {
					if (this.color == '') return
					setTimeout(() => {
						this.update(false)
					}, 0)
				},
				immediate: true
			},
		},
		/**
		 * 规则：如果没有配置expose，则methods中的方法均对外暴露，如果配置了expose，则以expose的配置为准向外暴露
		 * ['publicMethod'] 含义为：只有 `publicMethod` 在实例上可用
		 */
		expose: ['setCSS'],
		methods: {
			setSvg() {
				clearTimeout(this.timer)
				this.timer = setTimeout(()=> {
					const drawable = new PictureDrawable(this.svg!.renderToPicture())
					if ([null, ''].includes(this.color)) {
						this.$el?.setImageDrawable(drawable)
					} else {
						const newBM = createColoredBitmap(drawable, this.color)
						if(newBM == null) return
						this.$el?.setImageBitmap(newBM)
					}
				},20)
			},
			update(init : boolean) {
				if(this.svg == null) {
					setSvgImage(this.src, (svg) => {
						if(svg != null) {
							this.svg = svg
							this.setSvg()
							if(init) {
								this.$emit('load')
							}
							return
						} 
						this.$emit('error')
					})
				} else {
					this.setSvg()
				}

			},
			setCSS(css : string) {
				// this.$el?.setCSS(css)
			}
		},
		/**
		 * [可选实现] 组件被创建，组件第一个生命周期，
		 * 在内存中被占用的时候被调用，开发者可以在这里执行一些需要提前执行的初始化逻辑
		 */
		created() {},
		/**
		 * [可选实现] 对应平台的view载体即将被创建，对应前端beforeMount
		 */
		NVBeforeLoad() {},
		/**
		 * [必须实现] 创建原生View，必须定义返回值类型
		 * 开发者需要重点实现这个函数，声明原生组件被创建出来的过程，以及最终生成的原生组件类型
		 * （Android需要明确知道View类型，需特殊校验）
		 */
		NVLoad() : ImageView {
			let ImageView = new ImageView(this.$androidContext)
			ImageView.setOnClickListener(new ButtonClickListener(this));
			return ImageView
		},
		/**
		 * [可选实现] 原生View已创建
		 */
		NVLoaded() {},
		/**
		 * [可选实现] 原生View布局完成
		 */
		NVLayouted() {},
		/**
		 * [可选实现] 原生View将释放
		 */
		NVBeforeUnload() {},
		/**
		 * [可选实现] 原生View已释放，这里可以做释放View之后的操作
		 */
		NVUnloaded() {},
		/**
		 * [可选实现] 组件销毁
		 */
		unmounted() {},
		/**
		 * [可选实现] 自定组件布局尺寸，用于告诉排版系统，组件自身需要的宽高
		 * 一般情况下，组件的宽高应该是由终端系统的排版引擎决定，组件开发者不需要实现此函数
		 * 但是部分场景下，组件开发者需要自己维护宽高，则需要开发者重写此函数
		 */
		NVMeasure(size : UTSSize) : UTSSize {
			// size.width = 512.0.toFloat();
			// size.height = 512.0.toFloat();
			return size;
		}
	}
</script>

<style>

</style>