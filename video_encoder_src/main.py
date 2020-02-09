import argparse
import imageio_ffmpeg


VIDEO_PATH = "bad_apple_out.mp4"
CHUNK_OUTPUT = "chunks.bin"


def get_value_average(r, g, b):
    """Gets the average RGB value."""
    return int((r + g + b) / 3)


if __name__ == "__main__":
    # Get video reader
    reader = imageio_ffmpeg.read_frames(VIDEO_PATH)

    meta = reader.__next__()  # First item in generator is metadata.
    output = open(CHUNK_OUTPUT, mode="wb+")
    test = open("test.txt", mode="w")
    bits = b""

    print("")
    
    fnum = 0
    for frame in reader:
        print(f"\rFrame {fnum}")
        fnum += 1

        # print(list(frame))
        f = list(frame)
        # print(len(f))

        for i in range(0, len(f) // 3):
            r = f[i * 3]
            g = f[i * 3 + 1]
            b = f[i * 3 + 2]
            avg = get_value_average(r, g, b)
            bits += b"1" if avg > 127 else b"0"

            if fnum == 60:
                test.write("#" if avg < 128 else ".")

            if len(bits) == 8:
                output.write(int(bits[::-1], 2).to_bytes(1, "big"))
                bits = b""

    output.close()
    test.close()
    print("END")
