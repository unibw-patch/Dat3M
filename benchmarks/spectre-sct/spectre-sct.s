	.file	"spectre-sct.c"
	.intel_syntax noprefix
	.text
	.globl	idx_array_size
	.data
	.align 4
	.type	idx_array_size, @object
	.size	idx_array_size, 4
idx_array_size:
	.long	2
	.globl	idx_array
	.bss
	.type	idx_array, @object
	.size	idx_array, 2
idx_array:
	.zero	2
	.globl	publicarray
	.data
	.align 4
	.type	publicarray, @object
	.size	publicarray, 16
publicarray:
	.byte	0
	.byte	1
	.byte	2
	.byte	3
	.byte	4
	.byte	5
	.byte	6
	.byte	7
	.byte	8
	.byte	9
	.byte	10
	.byte	11
	.byte	12
	.byte	13
	.byte	14
	.byte	15
	.globl	publicarray2
	.align 4
	.type	publicarray2, @object
	.size	publicarray2, 16
publicarray2:
	.byte	20
	.zero	15
	.globl	secretarray
	.align 4
	.type	secretarray, @object
	.size	secretarray, 16
secretarray:
	.byte	10
	.byte	21
	.byte	32
	.byte	43
	.byte	54
	.byte	65
	.byte	76
	.byte	87
	.byte	98
	.byte	109
	.byte	110
	.byte	121
	.byte	-124
	.byte	-113
	.byte	-102
	.byte	-91
	.globl	temp
	.bss
	.type	temp, @object
	.size	temp, 1
temp:
	.zero	1
	.text
	.globl	victim_function_v1
	.type	victim_function_v1, @function
victim_function_v1:
.LFB6:
	.cfi_startproc
	sub	esp, 4
	.cfi_def_cfa_offset 8
	mov	eax, DWORD PTR [esp+8]
	mov	BYTE PTR [esp], al
	movzx	edx, BYTE PTR [esp]
	mov	eax, DWORD PTR idx_array_size
	cmp	edx, eax
	jnb	.L3
	mov	BYTE PTR idx_array, 64
	movzx	eax, BYTE PTR [esp]
	movzx	eax, BYTE PTR idx_array[eax]
	movzx	edx, al
	movzx	eax, BYTE PTR [esp]
	imul	eax, edx
	movzx	eax, BYTE PTR publicarray[eax]
	movzx	eax, al
	movzx	edx, BYTE PTR publicarray2[eax]
	movzx	eax, BYTE PTR temp
	and	eax, edx
	mov	BYTE PTR temp, al
.L3:
	nop
	add	esp, 4
	.cfi_def_cfa_offset 4
	ret
	.cfi_endproc
.LFE6:
	.size	victim_function_v1, .-victim_function_v1
	.globl	main
	.type	main, @function
main:
.LFB7:
	.cfi_startproc
	mov	eax, 0
	ret
	.cfi_endproc
.LFE7:
	.size	main, .-main
	.ident	"GCC: (Debian 8.3.0-6) 8.3.0"
	.section	.note.GNU-stack,"",@progbits
