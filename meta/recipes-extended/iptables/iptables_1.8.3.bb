SUMMARY = "Tools for managing kernel packet filtering capabilities"
DESCRIPTION = "iptables is the userspace command line program used to configure and control network packet \
filtering code in Linux."
HOMEPAGE = "http://www.netfilter.org/"
BUGTRACKER = "http://bugzilla.netfilter.org/"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263\
                    file://iptables/iptables.c;beginline=13;endline=25;md5=c5cffd09974558cf27d0f763df2a12dc"

SRC_URI = "http://netfilter.org/projects/iptables/files/iptables-${PV}.tar.bz2 \
           file://0001-configure-Add-option-to-enable-disable-libnfnetlink.patch \
           file://0002-configure.ac-only-check-conntrack-when-libnfnetlink-enabled.patch \
"

SRC_URI[md5sum] = "29de711d15c040c402cf3038c69ff513"
SRC_URI[sha256sum] = "a23cac034181206b4545f4e7e730e76e08b5f3dd78771ba9645a6756de9cdd80"

inherit autotools pkgconfig

EXTRA_OECONF = "--with-kernel=${STAGING_INCDIR}"

PACKAGECONFIG ?= "${@bb.utils.filter('DISTRO_FEATURES', 'ipv6', d)}"

PACKAGECONFIG[ipv6] = "--enable-ipv6,--disable-ipv6,"

# libnfnetlink recipe is in meta-networking layer
PACKAGECONFIG[libnfnetlink] = "--enable-libnfnetlink,--disable-libnfnetlink,libnfnetlink libnetfilter-conntrack"

# libnftnl recipe is in meta-networking layer(previously known as libnftables)
PACKAGECONFIG[libnftnl] = "--enable-nftables,--disable-nftables,libnftnl"

do_configure_prepend() {
	# Remove some libtool m4 files
	# Keep ax_check_linker_flags.m4 which belongs to autoconf-archive.
	rm -f libtool.m4 lt~obsolete.m4 ltoptions.m4 ltsugar.m4 ltversion.m4
}

PACKAGES += "${PN}-modules"
PACKAGES_DYNAMIC += "^${PN}-module-.*"

python populate_packages_prepend() {
    modules = do_split_packages(d, '${libdir}/xtables', r'lib(.*)\.so$', '${PN}-module-%s', '${PN} module %s', extra_depends='')
    if modules:
        metapkg = d.getVar('PN') + '-modules'
        d.appendVar('RDEPENDS_' + metapkg, ' ' + ' '.join(modules))
}

FILES_${PN} += "${datadir}/xtables"

# Include the symlinks as well in respective packages
FILES_${PN}-module-xt-conntrack += "${libdir}/xtables/libxt_state.so"
FILES_${PN}-module-xt-ct += "${libdir}/xtables/libxt_NOTRACK.so"

INSANE_SKIP_${PN}-module-xt-conntrack = "dev-so"
INSANE_SKIP_${PN}-module-xt-ct = "dev-so"

ALLOW_EMPTY_${PN}-modules = "1"

RDEPENDS_${PN} = "${PN}-module-xt-standard"
RRECOMMENDS_${PN} = " \
    ${PN}-modules \
    kernel-module-x-tables \
    kernel-module-ip-tables \
    kernel-module-iptable-filter \
    kernel-module-iptable-nat \
    kernel-module-nf-defrag-ipv4 \
    kernel-module-nf-conntrack \
    kernel-module-nf-conntrack-ipv4 \
    kernel-module-nf-nat \
    kernel-module-ipt-masquerade \
"
