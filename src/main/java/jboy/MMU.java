package jboy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MMU {
    int[] bios = new int[] { 0x31, 0xFE, 0xFF, 0xAF, 0x21, 0xFF, 0x9F, 0x32, 0xCB, 0x7C, 0x20, 0xFB, 0x21, 0x26, 0xFF,
            0x0E, 0x11, 0x3E, 0x80, 0x32, 0xE2, 0x0C, 0x3E, 0xF3, 0xE2, 0x32, 0x3E, 0x77, 0x77, 0x3E, 0xFC, 0xE0, 0x47,
            0x11, 0x04, 0x01, 0x21, 0x10, 0x80, 0x1A, 0xCD, 0x95, 0x00, 0xCD, 0x96, 0x00, 0x13, 0x7B, 0xFE, 0x34, 0x20,
            0xF3, 0x11, 0xD8, 0x00, 0x06, 0x08, 0x1A, 0x13, 0x22, 0x23, 0x05, 0x20, 0xF9, 0x3E, 0x19, 0xEA, 0x10, 0x99,
            0x21, 0x2F, 0x99, 0x0E, 0x0C, 0x3D, 0x28, 0x08, 0x32, 0x0D, 0x20, 0xF9, 0x2E, 0x0F, 0x18, 0xF3, 0x67, 0x3E,
            0x64, 0x57, 0xE0, 0x42, 0x3E, 0x91, 0xE0, 0x40, 0x04, 0x1E, 0x02, 0x0E, 0x0C, 0xF0, 0x44, 0xFE, 0x90, 0x20,
            0xFA, 0x0D, 0x20, 0xF7, 0x1D, 0x20, 0xF2, 0x0E, 0x13, 0x24, 0x7C, 0x1E, 0x83, 0xFE, 0x62, 0x28, 0x06, 0x1E,
            0xC1, 0xFE, 0x64, 0x20, 0x06, 0x7B, 0xE2, 0x0C, 0x3E, 0x87, 0xF2, 0xF0, 0x42, 0x90, 0xE0, 0x42, 0x15, 0x20,
            0xD2, 0x05, 0x20, 0x4F, 0x16, 0x20, 0x18, 0xCB, 0x4F, 0x06, 0x04, 0xC5, 0xCB, 0x11, 0x17, 0xC1, 0xCB, 0x11,
            0x17, 0x05, 0x20, 0xF5, 0x22, 0x23, 0x22, 0x23, 0xC9, 0xCE, 0xED, 0x66, 0x66, 0xCC, 0x0D, 0x00, 0x0B, 0x03,
            0x73, 0x00, 0x83, 0x00, 0x0C, 0x00, 0x0D, 0x00, 0x08, 0x11, 0x1F, 0x88, 0x89, 0x00, 0x0E, 0xDC, 0xCC, 0x6E,
            0xE6, 0xDD, 0xDD, 0xD9, 0x99, 0xBB, 0xBB, 0x67, 0x63, 0x6E, 0x0E, 0xEC, 0xCC, 0xDD, 0xDC, 0x99, 0x9F, 0xBB,
            0xB9, 0x33, 0x3E, 0x3c, 0x42, 0xB9, 0xA5, 0xB9, 0xA5, 0x42, 0x4C, 0x21, 0x04, 0x01, 0x11, 0xA8, 0x00, 0x1A,
            0x13, 0xBE, 0x20, 0xFE, 0x23, 0x7D, 0xFE, 0x34, 0x20, 0xF5, 0x06, 0x19, 0x78, 0x86, 0x23, 0x05, 0x20, 0xFB,
            0x86, 0x20, 0xFE, 0x3E, 0x01, 0xE0, 0x50 };

    String rom;
    int cart_type = 0;

    static class MBC {
        static int rom_bank = 0;
        static int ram_bank = 0;
        static int ram_on = 0;
        static int mode = 0;
    }

    int rom_offset = 0x4000;
    int ram_offset = 0x00;

    int[] eram = new int[32768];
    int[] wram = new int[8192];
    int[] zram = new int[127];

    boolean in_bios = true;
    int _if, ie = 0;

    Z80 z80;
    GPU gpu;
    Timer timer;
    KEY key;

    public MMU(Z80 z80, GPU gpu, Timer timer, KEY key) {
        this.z80 = z80;
        this.gpu = gpu;
        this.timer = timer;
        this.key = key;
    }

    public void setZ80(Z80 z80) {
        this.z80 = z80;
    }

    public void setGPU(GPU gpu) {
        this.gpu = gpu;
    }
    public void setTimer(Timer timer) {
        this.timer = timer;
    }
    public void setKEY(KEY key) {
        this.key = key;
    }

    public void reset() {
        /* Reset RAM */
        for (int i = 0; i < 32768; i++) {
            eram[i] = 0;
        }

        for (int i = 0; i < 8192; i++) {
            wram[i] = 0;
        }

        for (int i = 0; i < 127; i++) {
            zram[i] = 0;
        }

        /* Reset states */
        in_bios = true;
        // in_bios = false;
        ie = 0;
        _if = 0;

        cart_type = 0;
        MBC.rom_bank = 0;
        MBC.ram_bank = 0;
        MBC.ram_on = 0;
        MBC.mode = 0;

        rom_offset = 0x4000;
        ram_offset = 0x00;

        System.out.println("MMU has been reset");

    }

    public void load(File cart) {
        // try {
        //     rom = Files.readAllBytes(cart.toPath());
        // } catch (Exception e) {
        //     System.out.println("Invalid ROM. Loading nothing");
        // }

        try {
            rom = new String(Files.readAllBytes(cart.toPath()));
            cart_type = rom.charAt(0x0147);
            System.out.println("Loaded "+ rom.length() + " bytes.");

        } catch (Exception e) {
            //TODO: handle exception
            System.out.println("Invalid ROM. Loading nothing");
        }
    }

    public int rb(int addr) {
        // System.out.println(in_bios);
        switch (addr & 0xf000) {
        case 0:
            if (in_bios) {
                System.out.println(z80.r.pc);
                if (addr < 0x100) {
                    return bios[addr];
                } else if (z80.r.pc > 0x0100 ) {
                    in_bios = false;
                    System.out.println("Exiting BIOS");
                }

            } else {
                return rom.charAt(addr);
            }

        case 0x1000:
        case 0x2000:
        case 0x3000:
            return rom.charAt(addr);

        // ROM bank 1
        case 0x4000:
        case 0x5000:
        case 0x6000:
        case 0x7000:
            return rom.charAt(rom_offset + (addr & 0x3fff));

        // VRAM
        case 0x8000:
        case 0x9000:
            return gpu.vram[addr & 0x1FFF];

        // External RAM
        case 0xA000:
        case 0xB000:
            return eram[ram_offset + (addr & 0x3fff)];

        // Ram and Echo
        case 0xC000:
        case 0xD000:
        case 0xE000:
            return wram[addr & 0x1fff];

        // Everything else
        case 0xF000:

            // Echo RAM
            switch (addr & 0x0f00) {
            case 0x000:
            case 0x100:
            case 0x200:
            case 0x300:
            case 0x400:
            case 0x500:
            case 0x600:
            case 0x700:
            case 0x800:
            case 0x900:
            case 0xA00:
            case 0xB00:
            case 0xC00:
            case 0xD00:
                return wram[addr & 0x1fff];

            // OAM
            case 0xE00:
                return ((addr & 0xFF) < 0xA0) ? gpu.oam[addr & 0xFF] : 0;
            }

            // Zeropage RAM, I/O, interrupts
        case 0xF00:
            if (addr == 0xFFFF) {
                return ie;
            } else if (addr > 0xFF7F) {
                return zram[addr & 0x7F];
            } else
                switch (addr & 0xF0) {
                case 0x00:
                    switch (addr & 0xF) {
                    case 0:
                        return key.rb(); // JOYP
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        return timer.rb(addr);
                    case 15:
                        return _if; // Interrupt flags
                    default:
                        return 0;
                    }

                case 0x10:
                case 0x20:
                case 0x30:
                    return 0;

                case 0x40:
                case 0x50:
                case 0x60:
                case 0x70:
                    return gpu.rb(addr);
                }

        }

        return 0;
    }

    public int rw(int addr) {
        return (rb(addr) + rb(addr + 1) << 8);
    }

    public void wb(int addr, int val) {
        switch (addr & 0xF000) {
        // ROM bank 0
        // MBC1: Turn external RAM on
        case 0x0000:
        case 0x1000:
            switch (cart_type) {
            case 1:
                MBC.ram_on = ((val & 0xF) == 0xA) ? 1 : 0;
                break;
            }
            break;

        // MBC1: ROM bank switch
        case 0x2000:
        case 0x3000:
            switch (cart_type) {
            case 1:
                MBC.rom_bank &= 0x60;
                val &= 0x1F;
                if (val == 0)
                    val = 1;
                MBC.rom_bank |= val;
                rom_offset = MBC.rom_bank * 0x4000;
                break;
            }
            break;

        // ROM bank 1
        // MBC1: RAM bank switch
        case 0x4000:
        case 0x5000:
            switch (cart_type) {
            case 1:
                if (MBC.mode != 0) {
                    MBC.ram_bank = (val & 3);
                    ram_offset = MBC.ram_bank * 0x2000;
                } else {
                    MBC.rom_bank &= 0x1F;
                    MBC.rom_bank |= ((val & 3) << 5);
                    rom_offset = MBC.rom_bank * 0x4000;
                }
            }
            break;

        case 0x6000:
        case 0x7000:
            switch (cart_type) {
            case 1:
                MBC.mode = val & 1;
                break;
            }
            break;

        // VRAM
        case 0x8000:
        case 0x9000:
            gpu.vram[addr & 0x1FFF] = val;
            gpu.updateTile(addr & 0x1FFF, val);
            break;

        // External RAM
        case 0xA000:
        case 0xB000:
            eram[ram_offset + (addr & 0x1FFF)] = val;
            break;

        // Work RAM and echo
        case 0xC000:
        case 0xD000:
        case 0xE000:
            wram[addr & 0x1FFF] = val;
            break;

        // Everything else
        case 0xF000:
            switch (addr & 0x0F00) {
            // Echo RAM
            case 0x000:
            case 0x100:
            case 0x200:
            case 0x300:
            case 0x400:
            case 0x500:
            case 0x600:
            case 0x700:
            case 0x800:
            case 0x900:
            case 0xA00:
            case 0xB00:
            case 0xC00:
            case 0xD00:
                wram[addr & 0x1FFF] = val;
                break;

            // OAM
            case 0xE00:
                if ((addr & 0xFF) < 0xA0)
                    gpu.oam[addr & 0xFF] = val;
                gpu.updateOAM(addr, val);
                break;

            // Zeropage RAM, I/O, interrupts
            case 0xF00:
                if (addr == 0xFFFF) {
                    ie = val;
                } else if (addr > 0xFF7F) {
                    zram[addr & 0x7F] = val;
                } else
                    switch (addr & 0xF0) {
                    case 0x00:
                        switch (addr & 0xF) {
                        case 0:
                            key.wb(val);
                            break;
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                            timer.wb(addr, val);
                            break;
                        case 15:
                            _if = val;
                            break;
                        }
                        break;

                    case 0x10:
                    case 0x20:
                    case 0x30:
                        break;

                    case 0x40:
                    case 0x50:
                    case 0x60:
                    case 0x70:
                        gpu.wb(addr, val);
                        break;
                    }
            }
            break;
        }
    }

    public void ww(int addr, int val) {
        wb(addr, val & 255);
        wb(addr + 1, val >> 8);
    }
}